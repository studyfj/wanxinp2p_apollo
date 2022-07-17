package cn.itcast.wanxinp2p.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author fengjun
 * @version 1.0
 * @Email fengjun3@asiainfo.com
 * @date 2022/7/16 15:36
 * @Description 标的索引信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectIndexDTO {

    private Long id;
    // 发标人标识
    private Long consumerId;
    // 发表人用户编码
    private String userNo;
    // 标的编码
    private String projectNo;
    // 标的名称
    private String name;

    // 标的描述
    private String description;

    // 标的类型
    private String type;

    // 标的期限
    private Integer period;

    /**
     * 年化利率(投资人视图)
     */
    private BigDecimal annualRate;

    /**
     * 年化利率(借款人视图)
     */
    private BigDecimal borrowerAnnualRate;

    /**
     * 年化利率(平台佣金，利差)
     */
    private BigDecimal commissionAnnualRate;

    /**
     * 还款方式5.4.1
     */
    private String repaymentWay;

    /**
     * 募集金额
     */
    private BigDecimal amount;

    /**
     * 标的状态
     */
    private String projectStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 可用状态
     */
    private Integer status;

    /**
     * 是否是债权出让标
     */
    private Integer isAssignment;

}
