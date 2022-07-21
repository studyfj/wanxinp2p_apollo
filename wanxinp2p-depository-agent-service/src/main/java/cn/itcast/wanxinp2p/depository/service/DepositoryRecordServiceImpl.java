package cn.itcast.wanxinp2p.depository.service;

import cn.itcast.wanxinp2p.api.consumer.model.ConsumerRequest;
import cn.itcast.wanxinp2p.api.depository.model.*;
import cn.itcast.wanxinp2p.api.transaction.model.ProjectDTO;
import cn.itcast.wanxinp2p.common.cache.Cache;
import cn.itcast.wanxinp2p.common.domain.BusinessException;
import cn.itcast.wanxinp2p.common.domain.PreprocessBusinessTypeCode;
import cn.itcast.wanxinp2p.common.domain.StatusCode;
import cn.itcast.wanxinp2p.common.util.EncryptUtil;
import cn.itcast.wanxinp2p.common.util.RSAUtil;
import cn.itcast.wanxinp2p.depository.common.constant.DepositoryErrorCode;
import cn.itcast.wanxinp2p.depository.common.constant.DepositoryRequestTypeCode;
import cn.itcast.wanxinp2p.depository.entity.DepositoryRecord;
import cn.itcast.wanxinp2p.depository.mapper.DepositoryRecordMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/12 11:11
 * @Description 致敬大师，致敬未来的自己
 */
@Service
public class DepositoryRecordServiceImpl extends ServiceImpl<DepositoryRecordMapper, DepositoryRecord> implements DepositoryRecordService {

    @Autowired
    private ConfigService configService;

    @Autowired
    private OkHttpService okHttpService;

    @Override
    public GatewayRequest createConsumer(ConsumerRequest consumerRequest) {
        // 保存交易记录
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        this.baseMapper.insert(saveDepositoryRecord(consumerRequest));
        // 生成签名数据并返会给用户中心
        String requestStr = JSON.toJSONString(consumerRequest);
        //RSAUtil.sign(requestStr, RSAUtil.p2p_privateKey, "utf-8");
        String sign = RSAUtil.sign(requestStr, configService.getP2pPrivateKey(), "utf-8");
        GatewayRequest gatewayRequest = new GatewayRequest();
        // 交易名称
        gatewayRequest.setServiceName("PERSONAL_REGISTER");
        gatewayRequest.setPlatformNo(configService.getP2pCode());
        gatewayRequest.setReqData(EncryptUtil.encodeURL(EncryptUtil.encodeUTF8StringBase64(requestStr)));
        gatewayRequest.setSignature(EncryptUtil.encodeURL(sign));
        // 银行存管系统的地址
        gatewayRequest.setDepositoryUrl(configService.getDepositoryUrl() + "/gateway");
        return gatewayRequest;
    }

    private DepositoryRecord saveDepositoryRecord(ConsumerRequest consumerRequest) {
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        depositoryRecord.setRequestNo(consumerRequest.getRequestNo());
        // 交易类型 开户或者什么
        depositoryRecord.setRequestType(DepositoryRequestTypeCode.CONSUMER_CREATE.getCode());
        depositoryRecord.setObjectType("Consumer");
        depositoryRecord.setObjectId(consumerRequest.getId());
        depositoryRecord.setCreateDate(LocalDateTime.now());
        depositoryRecord.setRequestStatus(StatusCode.STATUS_OUT.getCode());
        return depositoryRecord;
    }

    private DepositoryRecord saveDepositoryRecord(DepositoryRecord depositoryRecord) {
        depositoryRecord.setCreateDate(LocalDateTime.now());
        depositoryRecord.setRequestStatus(StatusCode.STATUS_OUT.getCode());
        save(depositoryRecord);
        return depositoryRecord;
    }


    @Override
    public Boolean modifyRequestStatus(String requestNo, Integer requestsStatus) {

        return update(Wrappers.<DepositoryRecord>lambdaUpdate()
                .eq(DepositoryRecord::getRequestNo, requestNo)
                .set(DepositoryRecord::getRequestStatus, requestsStatus)
                .set(DepositoryRecord::getConfirmDate, LocalDateTime.now()));
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> createProject(ProjectDTO projectDTO) {

        // 保存交易记录
        DepositoryRecord depositoryRecord = new DepositoryRecord(projectDTO.getRequestNo(),
                DepositoryRequestTypeCode.CREATE.getCode(), "Project",
                projectDTO.getId());

        DepositoryResponseDTO<DepositoryBaseResponse> responseDTO = handleIdempotent(depositoryRecord);
        if (responseDTO != null) {
            return responseDTO;
        }
        // 根据requestNo获取交易记录
        depositoryRecord = getEntityByRequestNo(projectDTO.getRequestNo());
        // 签名数据
        // ProjectDTO 转换为 ProjectRequestDataDTO
        ProjectRequestDataDTO projectRequestDataDTO = convertProjectDTOToProjectRequestDataDTO(projectDTO, depositoryRecord.getRequestNo());
        String jsonStr = JSON.toJSONString(projectRequestDataDTO);
        String base64Sign = EncryptUtil.encodeUTF8StringBase64(jsonStr);
        // 往银行存管系统发送数据,标的信息
        // url地址 发送哪些数据
        String url = configService.getDepositoryUrl() + "/service";
        // 怎么发 OKHttpClient 发送Http请求
        return sendHttpGet("CREATE_PROJECT", url, base64Sign, depositoryRecord);

    }

    private DepositoryResponseDTO<DepositoryBaseResponse> sendHttpGet(String serviceName, String url, String reqData, DepositoryRecord depositoryRecord) {
        // 银行存管系统接收的4大参数: serviceName, platformNo, reqData, signature
        // signature会在okHttp拦截器(SignatureInterceptor)中处理
        // 平台编号
        String platformNo = configService.getP2pCode();
        // redData签名
        // 发送请求, 获取结果, 如果检验签名失败, 拦截器会在结果中放入: "signature", "false"
        String responseBody = okHttpService.doSyncGet(url + "?serviceName=" + serviceName + "&platformNo=" + platformNo + "&reqData=" + reqData);
        DepositoryResponseDTO<DepositoryBaseResponse> depositoryResponse = JSON.parseObject(responseBody, new TypeReference<DepositoryResponseDTO<DepositoryBaseResponse>>() {
        });
        // 响应后, 根据结果更新数据库( 进行签名判断 )
        depositoryRecord.setResponseData(responseBody);
        // 判断签名(signature)是为 false, 如果是说明验签失败!
        if (!"false".equals(depositoryResponse.getSignature())) {
            // 成功 - 设置数据同步状态
            depositoryRecord.setRequestStatus(StatusCode.STATUS_IN.getCode());
            // 设置消息确认时间
            depositoryRecord.setConfirmDate(LocalDateTime.now());
            // 更新数据库
            updateById(depositoryRecord);
        } else {
            // 失败 - 设置数据同步状态
            depositoryRecord.setRequestStatus(StatusCode.STATUS_FAIL.getCode());
            // 设置消息确认时间
            depositoryRecord.setConfirmDate(LocalDateTime.now());
            // 更新数据库
            updateById(depositoryRecord);
            // 抛业务异常
            throw new BusinessException(DepositoryErrorCode.E_160101);
        }
        return depositoryResponse;
    }

    private ProjectRequestDataDTO convertProjectDTOToProjectRequestDataDTO(
            ProjectDTO projectDTO, String requestNo) {
        if (projectDTO == null) {
            return null;
        }
        ProjectRequestDataDTO requestDataDTO = new ProjectRequestDataDTO();
        BeanUtils.copyProperties(projectDTO, requestDataDTO);
        requestDataDTO.setRequestNo(requestNo);
        return requestDataDTO;
    }

    /**
     * 保存交易记录
     */
    private DepositoryRecord saveDepositoryRecord(String requestNo, String requestType, String objectType, Long objectId) {
        DepositoryRecord depositoryRecord = new DepositoryRecord();
        // 设置请求流水号
        depositoryRecord.setRequestNo(requestNo);
        // 设置请求类型
        depositoryRecord.setRequestType(requestType);
        // 设置关联业务实体类型
        depositoryRecord.setObjectType(objectType);
        // 设置关联业务实体标识
        depositoryRecord.setObjectId(objectId);
        // 设置请求时间
        depositoryRecord.setCreateDate(LocalDateTime.now());
        // 设置数据同步状态
        depositoryRecord.setRequestStatus(StatusCode.STATUS_OUT.getCode());
        // 保存数据
        save(depositoryRecord);
        return depositoryRecord;
    }


    @Autowired
    private Cache cache;

    /**
     * 实现幂等性
     *
     * @param depositoryRecord
     * @return
     */
    private DepositoryResponseDTO<DepositoryBaseResponse> handleIdempotent(DepositoryRecord depositoryRecord) {
        //根据requestNo查询交易记录
        String requestNo = depositoryRecord.getRequestNo();
        DepositoryRecordDTO depositoryRecordDTO = getByRequestNo(requestNo);
        // 1.交易记录不存在则新增交易记录
        if (depositoryRecordDTO == null) {
            //保存交易记录
            saveDepositoryRecord(depositoryRecord);
            return null;
        }
        // 2.交易记录存在并且数据状态为未同步，则利用redis原子性自增，来争夺请求执行权
        if (StatusCode.STATUS_OUT.getCode() == depositoryRecordDTO.getRequestStatus()) {
            //如果requestNo不存在则返回1,如果已经存在,则会返回（requestNo已存在个数+1）
            Long count = cache.incrBy(requestNo, 1L);
            if (count == 1) {
                cache.expire(requestNo, 5); //设置requestNo有效期5秒
                return null;
            }
            // 若count大于1，说明已有线程在执行该操作，直接返回“正在处理”
            if (count > 1) {
                throw new BusinessException(DepositoryErrorCode.E_160103);
            }
        }
        // 3.交易记录存在并且数据状态为已同步，直接返回处理结果
        return JSON.parseObject(depositoryRecordDTO.getResponseData(),
                new TypeReference<DepositoryResponseDTO<DepositoryBaseResponse>>() {
                });
    }

    private DepositoryRecordDTO getByRequestNo(String requestNo) {
        DepositoryRecord depositoryRecord = getEntityByRequestNo(requestNo);
        if (depositoryRecord == null) {
            return null;
        }
        DepositoryRecordDTO depositoryRecordDTO = new DepositoryRecordDTO();
        BeanUtils.copyProperties(depositoryRecord, depositoryRecordDTO);
        return depositoryRecordDTO;
    }

    private DepositoryRecord getEntityByRequestNo(String requestNo) {
        return getOne(new QueryWrapper<DepositoryRecord>().lambda()
                .eq(DepositoryRecord::getRequestNo, requestNo));
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> userAutoPreTransaction(UserAutoPreTransactionRequest userAutoPreTransactionRequest) {
        // 保存交易记录(实现幂等性)
        DepositoryRecord depositoryRecord = new DepositoryRecord(userAutoPreTransactionRequest.getRequestNo(),
                userAutoPreTransactionRequest.getBizType(),
                "UserAutoPreTransactionRequest",
                userAutoPreTransactionRequest.getId());
        DepositoryResponseDTO<DepositoryBaseResponse> depository = handleIdempotent(depositoryRecord);
        if (depository != null) {
            return depository;
        }

        // 重新查一下 实现幂等性对数据进行了更改 对数据进行签名
        depositoryRecord = getEntityByRequestNo(userAutoPreTransactionRequest.getRequestNo());
        String jsonString = JSON.toJSONString(userAutoPreTransactionRequest);
        String encodeStr = EncryptUtil.encodeUTF8StringBase64(jsonString);
        // 发送数据到银行存管系统
        String depositoryUrl = configService.getDepositoryUrl() + "/service";
        return sendHttpGet("USER_AUTO_PRE_TRANSACTION", depositoryUrl, encodeStr, depositoryRecord);
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> confirmLoan(LoanRequest loanRequest) {
        DepositoryRecord depositoryRecord = new DepositoryRecord(loanRequest.getRequestNo(), DepositoryRequestTypeCode.FULL_LOAN.getCode(),
                "LoanRequest", loanRequest.getId());
        // 实习幂等性
        DepositoryResponseDTO<DepositoryBaseResponse> response = handleIdempotent(depositoryRecord);
        if (response != null) {
            return response;
        }
        depositoryRecord = getEntityByRequestNo(loanRequest.getRequestNo());

        String jsonString = JSON.toJSONString(loanRequest);
        String encodeStr = EncryptUtil.encodeUTF8StringBase64(jsonString);
        // 发送数据到银行存管系统
        String depositoryUrl = configService.getDepositoryUrl() + "/service";
        // 拦截器前进行签名
        return sendHttpGet("CONFIRM_LOAN", depositoryUrl, encodeStr, depositoryRecord);
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> modifyProjectStatus(ModifyProjectStatusDTO modifyProjectStatusDTO) {
        // arg2:请求参数类型
        DepositoryRecord depositoryRecord = new DepositoryRecord(modifyProjectStatusDTO.getRequestNo(), DepositoryRequestTypeCode.MODIFY_STATUS.getCode(),
                "Project",
                modifyProjectStatusDTO.getId());
        // 幂等性实现
        DepositoryResponseDTO<DepositoryBaseResponse> responseDTO = handleIdempotent(depositoryRecord);
        if (responseDTO != null) {
            return responseDTO;
        }
        // 根据requestNo获取交易记录
        depositoryRecord = getEntityByRequestNo(modifyProjectStatusDTO.getRequestNo());
        // loanRequest 转为 json 进行数据签名
        final String jsonString = JSON.toJSONString(modifyProjectStatusDTO);
        // 业务数据报文
        String reqData = EncryptUtil.encodeUTF8StringBase64(jsonString);
        // 拼接银行存管系统请求地址
        String url = configService.getDepositoryUrl() + "/service";
        // 封装通用方法, 请求银行存管系统
        return sendHttpGet("MODIFY_PROJECT", url, reqData, depositoryRecord);
    }

    @Override
    public DepositoryResponseDTO<DepositoryBaseResponse> confirmRepayment(RepaymentRequest repaymentRequest) {
        try {
            //构造交易记录
            DepositoryRecord depositoryRecord = new DepositoryRecord(repaymentRequest.getRequestNo(),
                    PreprocessBusinessTypeCode.REPAYMENT.getCode(), "Repayment", repaymentRequest.getId());

            // 分布式事务幂等性实现
            DepositoryResponseDTO<DepositoryBaseResponse> responseDTO =
                    handleIdempotent(depositoryRecord);
            if (responseDTO != null) {
                return responseDTO;
            }
            // 获取最新交易记录
            depositoryRecord =
                    getEntityByRequestNo(repaymentRequest.getRequestNo());
            /**
             * 确认还款(调用银行存管系统)
             */
            final String jsonString = JSON.toJSONString(repaymentRequest);
            // 业务数据报文, base64处理，方便传输
            String reqData = EncryptUtil.encodeUTF8StringBase64(jsonString);
            // 拼接银行存管系统请求地址
            String url = configService.getDepositoryUrl() + "/service";
            // 封装通用方法, 请求银行存管系统
            return sendHttpGet("CONFIRM_REPAYMENT", url, reqData, depositoryRecord);
        } catch (Exception e) {
            throw new BusinessException(DepositoryErrorCode.E_160101);
        }
    }

}
