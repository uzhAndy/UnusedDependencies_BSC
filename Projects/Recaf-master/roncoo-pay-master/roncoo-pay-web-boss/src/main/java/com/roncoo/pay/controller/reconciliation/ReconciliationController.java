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
package com.roncoo.pay.controller.reconciliation;

import com.roncoo.pay.common.core.dwz.DWZ;
import com.roncoo.pay.common.core.dwz.DwzAjax;
import com.roncoo.pay.common.core.enums.PayWayEnum;
import com.roncoo.pay.common.core.exception.BizException;
import com.roncoo.pay.common.core.page.PageBean;
import com.roncoo.pay.common.core.page.PageParam;
import com.roncoo.pay.reconciliation.entity.RpAccountCheckBatch;
import com.roncoo.pay.reconciliation.entity.RpAccountCheckMistake;
import com.roncoo.pay.reconciliation.entity.RpAccountCheckMistakeScratchPool;
import com.roncoo.pay.reconciliation.enums.MistakeHandleStatusEnum;
import com.roncoo.pay.reconciliation.enums.ReconciliationMistakeTypeEnum;
import com.roncoo.pay.reconciliation.service.RpAccountCheckBatchService;
import com.roncoo.pay.reconciliation.service.RpAccountCheckMistakeScratchPoolService;
import com.roncoo.pay.reconciliation.service.RpAccountCheckMistakeService;
import com.roncoo.pay.reconciliation.service.RpAccountCheckTransactionService;
import com.roncoo.pay.trade.enums.TradeStatusEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * ???????????????.
 * 
 * @company??????????????????????????????????????????????????????:www.roncoo.com???
 * @author???Along.shen
 *
 */
@Controller
@RequestMapping("/reconciliation")
public class ReconciliationController {

	private static final Log log = LogFactory.getLog(ReconciliationController.class);

	@Autowired
	private RpAccountCheckBatchService rpAccountCheckBatchService;

	@Autowired
	private RpAccountCheckTransactionService rpAccountCheckTransactionService;

	@Autowired
	private RpAccountCheckMistakeService rpAccountCheckMistakeService;

	@Autowired
	private RpAccountCheckMistakeScratchPoolService rpAccountCheckMistakeScratchPoolService;

	/**
	 * ????????????????????????
	 * 
	 * @param pageParam
	 * @param checkbatch
	 * @param model
	 * @return
	 */
	@RequiresPermissions("recon:batch:view")
	@RequestMapping(value = "/list/checkbatch")
	public String listCheckbatch(PageParam pageParam, RpAccountCheckBatch checkbatch, Model model, String billDay) {
		try {
			// ????????????????????????
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("billDate", billDay);
			PageBean pageBean = rpAccountCheckBatchService.listPage(pageParam, paramMap);
			model.addAttribute("pageBean", pageBean);
			model.addAttribute("pageParam", pageParam);
			model.addAttribute("billDay", billDay);

		} catch (Exception e) {
			log.error(e);
			// ?????????form????????????????????????,???new??????DwzAjax??????
			DwzAjax dwz = new DwzAjax();
			dwz.setStatusCode(DWZ.ERROR);
			dwz.setMessage("??????????????????????????????????????????????????????????????????");
			model.addAttribute("dwz", dwz);
			return "common/ajaxDone";
		}
		return "reconciliation/batch/list";
	}

	/**
	 * ????????????????????????
	 * 
	 * @param pageParam
	 * @param checkbatch
	 * @param model
	 * @return
	 */
	@RequiresPermissions("recon:mistake:view")
	@RequestMapping(value = "/list/mistake")
	public String listMistake(PageParam pageParam, RpAccountCheckMistake mistake, String billBeginDate, String billEndDate, Model model) {
		try {
			// ????????????????????????
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("beginDate", billBeginDate);
			paramMap.put("endDate", billEndDate);

			PageBean pageBean = rpAccountCheckMistakeService.listPage(pageParam, paramMap);
			model.addAttribute("billBeginDate", billBeginDate);
			model.addAttribute("billEndDate", billEndDate);
			model.addAttribute("pageBean", pageBean);
			model.addAttribute("pageParam", pageParam);
			model.addAttribute("tradeStatusEnums", TradeStatusEnum.toList());
			model.addAttribute("payWayEnums", PayWayEnum.toList());
			model.addAttribute("mistakeHandleStatusEnums", MistakeHandleStatusEnum.toList());
			model.addAttribute("reconciliationMistakeTypeEnums", ReconciliationMistakeTypeEnum.toList());

		} catch (Exception e) {
			log.error(e);
			// ?????????form????????????????????????,???new??????DwzAjax??????
			DwzAjax dwz = new DwzAjax();
			dwz.setStatusCode(DWZ.ERROR);
			dwz.setMessage("??????????????????????????????????????????????????????????????????");
			model.addAttribute("dwz", dwz);
			return "common/ajaxDone";
		}
		return "reconciliation/mistake/list";
	}

	/**
	 * ???????????????????????????
	 * 
	 * @param pageParam
	 * @param checkbatch
	 * @param model
	 * @return
	 */
	@RequiresPermissions("recon:scratchPool:view")
	@RequestMapping(value = "/list/scratchPool")
	public String listScratchPool(PageParam pageParam, RpAccountCheckMistakeScratchPool scratchRecord, Model model) {
		try {
			// ?????????????????????????????????
			PageBean pageBean = rpAccountCheckMistakeScratchPoolService.listPage(pageParam, scratchRecord);

			model.addAttribute("pageBean", pageBean);
			model.addAttribute("pageParam", pageParam);

		} catch (Exception e) {
			log.error(e);
			// ?????????form????????????????????????,???new??????DwzAjax??????
			DwzAjax dwz = new DwzAjax();
			dwz.setStatusCode(DWZ.ERROR);
			dwz.setMessage("??????????????????????????????????????????????????????????????????");
			model.addAttribute("dwz", dwz);
			return "common/ajaxDone";
		}
		return "reconciliation/scratchPool/list";
	}

	/**
	 * ????????????????????????
	 * 
	 * @param pageParam
	 * @param checkbatch
	 * @param model
	 * @return
	 */
	@RequiresPermissions("recon:mistake:view")
	@RequestMapping(value = "/mistake/tohandlePage")
	public String toHandlePage(Model model, HttpServletRequest request, @RequestParam("id") String id) {
		RpAccountCheckMistake mistake = rpAccountCheckMistakeService.getDataById(id);
		model.addAttribute("mistake", mistake);
		model.addAttribute("reconciliationMistakeTypeEnums", ReconciliationMistakeTypeEnum.toList());
		model.addAttribute("tradeStatusEnums", TradeStatusEnum.toList());
		return "reconciliation/mistake/handlePage";
	}

	/**
	 * ??????????????????
	 * 
	 * @param dwz
	 * @param model
	 * @param request
	 * @param id
	 *            ??????id
	 * @param handleType
	 *            ????????????(???????????????????????????)
	 * @param handleRemark
	 *            ????????????
	 * @return
	 */
	@RequiresPermissions("recon:mistake:edit")
	@RequestMapping(value = "/mistake/handle")
	public String handleMistake(DwzAjax dwz, Model model, HttpServletRequest request, @RequestParam("id") String id, @RequestParam("handleType") String handleType, @RequestParam("handleRemark") String handleRemark) {
		try {
			// ??????????????????
			rpAccountCheckTransactionService.handle(id, handleType, handleRemark);
		} catch (BizException e) {
			log.error(e);
			dwz.setStatusCode(DWZ.ERROR);
			dwz.setMessage(e.getMsg());
			model.addAttribute("dwz", dwz);
			return "common/ajaxDone";
		} catch (Exception e) {
			log.error(e);
			dwz.setStatusCode(DWZ.ERROR);
			dwz.setMessage("??????????????????????????????????????????????????????");
			model.addAttribute("dwz", dwz);
			return "common/ajaxDone";
		}
		dwz.setStatusCode(DWZ.SUCCESS);
		dwz.setMessage("???????????????");
		model.addAttribute("dwz", dwz);
		return "common/ajaxDone";
	}
}
