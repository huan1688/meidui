package com.meiduimall.service.settlement.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class EcmMzfDraw implements Serializable{
	
	private static final long serialVersionUID = 1437673631589990312L;

	private Integer id;

    // 提现编号
    private String drawCode;

    // 角色类型 1区代 2个代 3商家
    private Integer drawType;
    
    // 代理和商家的编号
    private String code;
    
    // 账号类型1公司，2个人
    private Integer userType;
    
    //收款人姓名
    private String realname;

    //银行名称
    private String bankname;

    //银行卡号
    private String banknum;

    //银行地址
    private String bankaddress;

    //分行地址
    private String bankBranch;

    //提现金额
    private BigDecimal money;

    //提现手续费
    private BigDecimal cashWithdrawalFee;

    //总金额
    private BigDecimal totalMoney;

    //提现时间
    private Integer addTime;
    
    //提现状态1待审核，2审核通过，3审核不通过
    private Integer status;

    //驳回原因
    private String remark;

    //操作时间
    private Integer drawTime;

    //审核人id(admin)
    private Integer verifyId;

    //审核人(运营)
    private String verifyName;

    //审核时间
    private Integer verifyTime;

    //1待审核，2审核通过，3审核不通过
    private Integer verifyStatus;

    //财务审核人id(admin)
    private Integer financeId;

    //财务经办
    private String financeName;

    private Integer financeTime;

    //1待审核，4转账成功,5失败
    private Integer financeStatus;

    //操作方式：人工，系统
    private String drawName;

    private Integer addTimeStart;
    
	private Integer addTimeEnd;
    
	private Integer type;  //成功或失败。

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getAddTimeStart() {
		return addTimeStart;
	}

	public void setAddTimeStart(Integer addTimeStart) {
		this.addTimeStart = addTimeStart;
	}

	public Integer getAddTimeEnd() {
		return addTimeEnd;
	}

	public void setAddTimeEnd(Integer addTimeEnd) {
		this.addTimeEnd = addTimeEnd;
	}

	public EcmMzfDraw() {
		super();
	}

	public EcmMzfDraw(String drawCode) {
		super();
		this.drawCode = drawCode;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDrawCode() {
        return drawCode;
    }

    public void setDrawCode(String drawCode) {
        this.drawCode = drawCode == null ? null : drawCode.trim();
    }

     
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

     
    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname == null ? null : realname.trim();
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname == null ? null : bankname.trim();
    }

    public String getBanknum() {
        return banknum;
    }

    public void setBanknum(String banknum) {
        this.banknum = banknum == null ? null : banknum.trim();
    }

    public String getBankaddress() {
        return bankaddress;
    }

    public void setBankaddress(String bankaddress) {
        this.bankaddress = bankaddress == null ? null : bankaddress.trim();
    }

    public String getBankBranch() {
        return bankBranch;
    }

    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch == null ? null : bankBranch.trim();
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public BigDecimal getCashWithdrawalFee() {
        return cashWithdrawalFee;
    }

    public void setCashWithdrawalFee(BigDecimal cashWithdrawalFee) {
        this.cashWithdrawalFee = cashWithdrawalFee;
    }

    public BigDecimal getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(BigDecimal totalMoney) {
        this.totalMoney = totalMoney;
    }

    public Integer getAddTime() {
        return addTime;
    }

    public void setAddTime(Integer addTime) {
        this.addTime = addTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Integer getDrawTime() {
        return drawTime;
    }

    public void setDrawTime(Integer drawTime) {
        this.drawTime = drawTime;
    }

    public Integer getVerifyId() {
        return verifyId;
    }

    public void setVerifyId(Integer verifyId) {
        this.verifyId = verifyId;
    }

    public String getVerifyName() {
        return verifyName;
    }

    public void setVerifyName(String verifyName) {
        this.verifyName = verifyName == null ? null : verifyName.trim();
    }

    public Integer getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(Integer verifyTime) {
        this.verifyTime = verifyTime;
    }

    public Integer getVerifyStatus() {
		return verifyStatus;
	}

	public void setVerifyStatus(Integer verifyStatus) {
		this.verifyStatus = verifyStatus;
	}

	public Integer getFinanceId() {
        return financeId;
    }

    public void setFinanceId(Integer financeId) {
        this.financeId = financeId;
    }

    public String getFinanceName() {
        return financeName;
    }

    public void setFinanceName(String financeName) {
        this.financeName = financeName == null ? null : financeName.trim();
    }

    public Integer getFinanceTime() {
        return financeTime;
    }

    public void setFinanceTime(Integer financeTime) {
        this.financeTime = financeTime;
    }

     

    public Integer getDrawType() {
		return drawType;
	}

	public void setDrawType(Integer drawType) {
		this.drawType = drawType;
	}

	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getFinanceStatus() {
		return financeStatus;
	}

	public void setFinanceStatus(Integer financeStatus) {
		this.financeStatus = financeStatus;
	}

	public String getDrawName() {
        return drawName;
    }

    public void setDrawName(String drawName) {
        this.drawName = drawName == null ? null : drawName.trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", drawCode=").append(drawCode);
        sb.append(", drawType=").append(drawType);
        sb.append(", code=").append(code);
        sb.append(", userType=").append(userType);
        sb.append(", realname=").append(realname);
        sb.append(", bankname=").append(bankname);
        sb.append(", banknum=").append(banknum);
        sb.append(", bankaddress=").append(bankaddress);
        sb.append(", bankBranch=").append(bankBranch);
        sb.append(", money=").append(money);
        sb.append(", cashWithdrawalFee=").append(cashWithdrawalFee);
        sb.append(", totalMoney=").append(totalMoney);
        sb.append(", addTime=").append(addTime);
        sb.append(", status=").append(status);
        sb.append(", remark=").append(remark);
        sb.append(", drawTime=").append(drawTime);
        sb.append(", verifyId=").append(verifyId);
        sb.append(", verifyName=").append(verifyName);
        sb.append(", verifyTime=").append(verifyTime);
        sb.append(", verifyStatus=").append(verifyStatus);
        sb.append(", financeId=").append(financeId);
        sb.append(", financeName=").append(financeName);
        sb.append(", financeTime=").append(financeTime);
        sb.append(", financeStatus=").append(financeStatus);
        sb.append(", drawName=").append(drawName);
        sb.append("]");
        return sb.toString();
    }
}
 
