/*
 * Copyright 2015-2102 RonCoo(http://www.roncoo.com) Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.roncoo.pay.account.service.impl;

import com.roncoo.pay.account.dao.RpSettDailyCollectDao;
import com.roncoo.pay.account.dao.RpSettRecordDao;
import com.roncoo.pay.account.entity.RpAccount;
import com.roncoo.pay.account.entity.RpSettDailyCollect;
import com.roncoo.pay.account.entity.RpSettRecord;
import com.roncoo.pay.account.enums.SettDailyCollectStatusEnum;
import com.roncoo.pay.account.enums.SettDailyCollectTypeEnum;
import com.roncoo.pay.account.enums.SettModeTypeEnum;
import com.roncoo.pay.account.enums.SettRecordStatusEnum;
import com.roncoo.pay.account.exception.AccountBizException;
import com.roncoo.pay.account.exception.SettBizException;
import com.roncoo.pay.account.service.RpAccountQueryService;
import com.roncoo.pay.account.service.RpAccountTransactionService;
import com.roncoo.pay.account.service.RpSettHandleService;
import com.roncoo.pay.account.utils.AccountConfigUtil;
import com.roncoo.pay.account.vo.DailyCollectAccountHistoryVo;
import com.roncoo.pay.common.core.exception.BizException;
import com.roncoo.pay.common.core.utils.DateUtils;
import com.roncoo.pay.trade.enums.TrxTypeEnum;
import com.roncoo.pay.user.entity.RpUserBankAccount;
import com.roncoo.pay.user.entity.RpUserInfo;
import com.roncoo.pay.user.enums.BankAccountTypeEnum;
import com.roncoo.pay.user.exception.UserBizException;
import com.roncoo.pay.user.service.RpUserBankAccountService;
import com.roncoo.pay.user.service.RpUserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * ?????????????????????????????????
 * ???????????????www.roncoo.com
 * @author???zenghao
 */
@Service("rpSettHandleService")
public class RpSettHandleServiceImpl implements RpSettHandleService {
	@Autowired
	private RpSettDailyCollectDao rpSettDailyCollectDao;
	@Autowired
	private RpSettRecordDao rpSettRecordDao;
	@Autowired
	private RpAccountTransactionService rpAccountTransactionService;
	@Autowired
	private RpAccountQueryService rpAccountQueryService;
	@Autowired
	private RpUserInfoService rpUserInfoService;
	@Autowired
	private RpUserBankAccountService rpUserBankAccountService;

	/**
	 * ??????????????????????????????????????????????????????.<br/>
	 * 
	 * @param userNo
	 *            ????????????.
	 * @param endDate
	 *            ??????????????????.
	 * @param riskDay
	 *            ???????????????.
	 * @param userName
	 *            ????????????
	 * @param codeNum
	 *            ????????????
	 */
	@Transactional(rollbackFor = Exception.class)
	public void dailySettlementCollect(String userNo, Date endDate, int riskDay, String userName){
		// ??????????????????????????????
		RpAccount account = rpAccountQueryService.getAccountByUserNo(userNo);
		// ????????????
		String endDateStr = DateUtils.formatDate(endDate, "yyyy-MM-dd");
		// ??????????????????
		List<DailyCollectAccountHistoryVo> accountHistoryList = rpAccountQueryService.listDailyCollectAccountHistoryVo(account.getAccountNo(), endDateStr, riskDay, null);
		// ????????????
		BigDecimal totalAmount = BigDecimal.ZERO;
		for (DailyCollectAccountHistoryVo collectVo : accountHistoryList) {
			// ?????????????????????
			totalAmount = totalAmount.add(collectVo.getTotalAmount());
			// ????????????????????????
			RpSettDailyCollect dailyCollect = new RpSettDailyCollect();
			dailyCollect.setAccountNo(collectVo.getAccountNo());
			dailyCollect.setUserName(userName);
			dailyCollect.setCollectDate(collectVo.getCollectDate());
			dailyCollect.setCollectType(SettDailyCollectTypeEnum.ALL.name());
			dailyCollect.setTotalAmount(collectVo.getTotalAmount());
			dailyCollect.setTotalCount(collectVo.getTotalNum());
			dailyCollect.setSettStatus(SettDailyCollectStatusEnum.SETTLLED.name());
			dailyCollect.setRiskDay(collectVo.getRiskDay());
			dailyCollect.setRemark("");
			dailyCollect.setEditTime(new Date());
			rpSettDailyCollectDao.insert(dailyCollect);
		}

		// ??????????????????????????????????????????????????????????????????
		rpAccountTransactionService.settCollectSuccess(userNo, endDateStr, riskDay, totalAmount);
	}
	
	/**
	 * ????????????--???????????????
	 * 
	 * @param userNo
	 * @param accountNo
	 * @param settAmount
	 * @param bankAccount
	 */
	public void launchSett(String userNo, BigDecimal settAmount){
		RpAccount account = rpAccountQueryService.getAccountByUserNo(userNo);
		RpUserInfo userInfo = rpUserInfoService.getDataByMerchentNo(userNo);
		RpUserBankAccount rpUserBankAccount = rpUserBankAccountService.getByUserNo(userNo);
		BigDecimal availableAmount = account.getAvailableSettAmount();
		if (settAmount.compareTo(availableAmount) > 0) {
			// ????????????
			throw AccountBizException.ACCOUNT_SUB_AMOUNT_OUTLIMIT;
		}
		if (rpUserBankAccount == null) {
			throw UserBizException.USER_BANK_ACCOUNT_IS_NULL;
			
		}
		
		String settType = SettModeTypeEnum.SELFHELP_SETTLE.name();
		this.launchSett(userNo, userInfo.getUserName(), account.getAccountNo(), settAmount, rpUserBankAccount, settType);
		
	}

	/**
	 * ????????????
	 * 
	 * @param userNo
	 * @param accountNo
	 * @param settAmount
	 * @param bankAccount
	 * @param settType ??????????????????:???????????????
	 */

	@Transactional(rollbackFor = Exception.class)
	private void launchSett(String userNo, String userName, String accountNo, BigDecimal settAmount, RpUserBankAccount bankAccount, String settType) {
		
		// ??????????????????
		RpSettRecord settRecord = new RpSettRecord();
		settRecord.setAccountNo(accountNo);
		settRecord.setCountry("??????");
		settRecord.setProvince(bankAccount.getProvince());
		settRecord.setCity(bankAccount.getCity());
		settRecord.setAreas(bankAccount.getAreas());
		settRecord.setBankAccountAddress(bankAccount.getStreet());
		settRecord.setBankAccountName(bankAccount.getBankAccountName());
		settRecord.setBankCode(bankAccount.getBankCode());
		settRecord.setBankName(bankAccount.getBankName());
		settRecord.setBankAccountNo(bankAccount.getBankAccountNo());
		settRecord.setBankAccountType(bankAccount.getBankAccountType());
		settRecord.setOperatorLoginname("");
		settRecord.setOperatorRealname("");
		settRecord.setRemitAmount(settAmount);
		settRecord.setRemitRequestTime(new Date());
		settRecord.setSettAmount(settAmount);
		settRecord.setSettFee(BigDecimal.ZERO);
		settRecord.setSettMode(settType);
		settRecord.setSettStatus(SettRecordStatusEnum.WAIT_CONFIRM.name());
		settRecord.setUserName(userName);
		settRecord.setUserNo(userNo);
		settRecord.setMobileNo(bankAccount.getMobileNo());
		settRecord.setEditTime(new Date());
		rpSettRecordDao.insert(settRecord);

		// ?????????????????????????????????
		rpAccountTransactionService.freezeAmount(userNo, settAmount);
	}

	/**
	 * ??????????????????
	 * 
	 * @param userNo
	 */
	public void launchAutoSett(String userNo){
		RpUserInfo userInfo = rpUserInfoService.getDataByMerchentNo(userNo);
		RpAccount account = rpAccountQueryService.getAccountByUserNo(userNo);
		BigDecimal settAmount = account.getAvailableSettAmount();
		String settMinAmount = AccountConfigUtil.readConfig("sett_min_amount");
		if (settAmount.compareTo(new BigDecimal(settMinAmount)) == -1) {
			throw new BizException("???????????????????????????????????????:" + settMinAmount);
		}

		RpUserBankAccount rpUserBankAccount = rpUserBankAccountService.getByUserNo(userNo);
		if (rpUserBankAccount == null) {
			throw new BizException("?????????????????????????????????????????????????????????");
		}

		// ????????????????????????????????????????????????
		String bankType = rpUserBankAccount.getBankAccountType();

		// ???????????????????????????????????? 1.????????????????????????5W 
		if (bankType.equals(BankAccountTypeEnum.PRIVATE_DEBIT_ACCOUNT.name())) {
			// ????????????????????????
			String settMaxAmount = AccountConfigUtil.readConfig("sett_max_amount");
			if (settAmount.compareTo(new BigDecimal(settMaxAmount)) == 1) {
				throw new BizException("???????????????????????????????????????:" + settMaxAmount);
			}
		}
		// ??????????????????userNo???????????????????????????
		String userName = userInfo.getUserName();
		String accountNo = account.getAccountNo();
		String settType = SettModeTypeEnum.REGULAR_SETTLE.name();
		this.launchSett(userNo, userName, accountNo, settAmount, rpUserBankAccount,settType);
	}

	/**
	 * ????????????
	 */
	public void audit(String settId, String settStatus, String remark){
		RpSettRecord settRecord = rpSettRecordDao.getById(settId);
		if(!settRecord.getSettStatus().equals(SettRecordStatusEnum.WAIT_CONFIRM.name())){
			throw SettBizException.SETT_STATUS_ERROR;
		}
		settRecord.setSettStatus(settStatus);
		settRecord.setEditTime(new Date());
		settRecord.setRemark(remark);
		rpSettRecordDao.update(settRecord);
		
		if(settStatus.equals(SettRecordStatusEnum.CANCEL.name())){//???????????????
			//????????????
			rpAccountTransactionService.unFreezeSettAmount(settRecord.getUserNo(), settRecord.getSettAmount());
		}
	}
	
	/**
	 * ??????
	 */
	@Transactional(rollbackFor = Exception.class)
	public void remit(String settId, String settStatus, String remark){
		RpSettRecord settRecord = rpSettRecordDao.getById(settId);
		if(!settRecord.getSettStatus().equals(SettRecordStatusEnum.CONFIRMED.name())){
			throw SettBizException.SETT_STATUS_ERROR;
		}
		settRecord.setSettStatus(settStatus);
		settRecord.setEditTime(new Date());
		settRecord.setRemitRemark(remark);
		settRecord.setRemitAmount(settRecord.getSettAmount());
		settRecord.setRemitConfirmTime(new Date());
		settRecord.setRemitRequestTime(new Date());
		rpSettRecordDao.update(settRecord);
		
		if(settStatus.equals(SettRecordStatusEnum.REMIT_FAIL.name())){//????????????
			//????????????
			rpAccountTransactionService.unFreezeSettAmount(settRecord.getUserNo(), settRecord.getSettAmount());
		}else if(settStatus.equals(SettRecordStatusEnum.REMIT_SUCCESS.name())){
			rpAccountTransactionService.unFreezeAmount(settRecord.getUserNo(), settRecord.getSettAmount(), settRecord.getId(), TrxTypeEnum.REMIT.name(), remark);
		}
	}
}
