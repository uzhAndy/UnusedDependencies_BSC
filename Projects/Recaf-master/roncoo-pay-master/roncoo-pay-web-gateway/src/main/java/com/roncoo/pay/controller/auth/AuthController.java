package com.roncoo.pay.controller.auth;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.roncoo.pay.common.core.utils.StringUtil;
import com.roncoo.pay.controller.common.BaseController;
import com.roncoo.pay.service.CnpPayService;
import com.roncoo.pay.trade.entity.RpUserBankAuth;
import com.roncoo.pay.trade.enums.TradeStatusEnum;
import com.roncoo.pay.trade.exception.TradeBizException;
import com.roncoo.pay.trade.service.RpTradePaymentManagerService;
import com.roncoo.pay.trade.service.RpUserBankAuthService;
import com.roncoo.pay.trade.utils.MerchantApiUtil;
import com.roncoo.pay.trade.utils.auth.AuthConfigUtil;
import com.roncoo.pay.trade.vo.*;
import com.roncoo.pay.user.entity.RpUserPayConfig;
import com.roncoo.pay.user.exception.UserBizException;
import com.roncoo.pay.user.service.RpUserPayConfigService;
import com.roncoo.pay.utils.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping(value = "/auth")
public class AuthController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private static final String CONTENT_TYPE = "text/text;charset=UTF-8";

    @Autowired
    private RpUserPayConfigService userPayConfigService;
    @Autowired
    private CnpPayService cnpPayService;
    @Autowired
    private RpTradePaymentManagerService tradePaymentManagerService;
    @Autowired
    private RpUserBankAuthService userBankAuthService;

    @RequestMapping(value = "/initAuth")
    public String initPay(@Valid AuthParamVo paramVo, BindingResult bindingResult, HttpServletRequest request, ModelMap modelMap) {

        RpUserPayConfig userPayConfig = cnpPayService.checkParamAndGetUserPayConfig(paramVo,  bindingResult, request);

        String productName = "????????????";
        String orderIp = null;
        try {
            orderIp = NetworkUtil.getIpAddress(request);
        } catch (IOException e) {
            logger.info("??????Ip??????!");
        }
        if (StringUtil.isEmpty(orderIp)) {
            orderIp = "127.0.0.1";
        }
        AuthInitResultVo resultVo = tradePaymentManagerService.initDirectAuth(productName, BigDecimal.valueOf(Double.valueOf(AuthConfigUtil.AUTH_AMOUNT)), orderIp, paramVo, userPayConfig);
        if (TradeStatusEnum.SUCCESS.equals(resultVo.getTradeStatus()) && resultVo.isAuth()) {
            return "redirect:/auth/doAuth/" + resultVo.getMerchantNo() + "/" + resultVo.getMerchantOrderNo();
        }
        RpPayGateWayPageShowVo payGateWayPageShowVo = new RpPayGateWayPageShowVo();
        payGateWayPageShowVo.setMerchantName(resultVo.getMerchantName());
        payGateWayPageShowVo.setMerchantOrderNo(resultVo.getMerchantOrderNo());
        payGateWayPageShowVo.setOrderAmount(resultVo.getOrderAmount());
        payGateWayPageShowVo.setPayKey(resultVo.getPayKey());
        payGateWayPageShowVo.setProductName(resultVo.getProductName());
        payGateWayPageShowVo.setPayTypeEnumMap(resultVo.getPayTypeEnumMap());
        modelMap.addAttribute("payGateWayPageShowVo", payGateWayPageShowVo);//????????????????????????
        return "gateway";
    }

    @RequestMapping(value = "/initProgramAuth")
    @ResponseBody
    public void initProgramAuth(@Valid AuthProgramInitParamVo paramVo, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response) {
        String payResultJson;
        try {
            //????????????
            if (bindingResult.hasErrors()) {
                String errMsg = getErrorMsg(bindingResult);
                logger.info("?????????????????????--?????????????????????[{}]", errMsg);
                throw new TradeBizException(TradeBizException.TRADE_PARAM_ERROR, errMsg);
            }

            RpUserPayConfig userPayConfig = userPayConfigService.getByPayKey(paramVo.getPayKey());
            if (userPayConfig == null) {
                throw new UserBizException(UserBizException.USER_PAY_CONFIG_ERRPR, "????????????????????????");
            }
            //ip??????
            cnpPayService.checkIp(userPayConfig, request);

            //??????
            Map<String, Object> paramMap = JSONObject.parseObject(JSON.toJSONString(paramVo));
            paramMap.remove("sign");
            if (!MerchantApiUtil.isRightSign(paramMap, userPayConfig.getPaySecret(), paramVo.getSign())) {
                throw new TradeBizException(TradeBizException.TRADE_ORDER_ERROR, "??????????????????");
            }

            String productName = "????????????";
            String orderIp = null;
            try {
                orderIp = NetworkUtil.getIpAddress(request);
            } catch (IOException e) {
                logger.info("??????Ip??????!");
            }
            if (StringUtil.isEmpty(orderIp)) {
                orderIp = "127.0.0.1";
            }

            AuthProgramInitResultVo resultVo = tradePaymentManagerService.initProgramDirectAuth(productName, BigDecimal.valueOf(Double.valueOf(AuthConfigUtil.AUTH_AMOUNT)), orderIp, paramVo, userPayConfig);
            payResultJson = JSONObject.toJSONString(resultVo);
        } catch (Exception e) {
            JSONObject resultJson = new JSONObject();
            resultJson.put("errCode", "NO");
            resultJson.put("bankReturnMsg", e.getMessage());
            payResultJson = resultJson.toJSONString();
        }

        logger.debug("?????????--?????????????????????==>{}", payResultJson);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(200);
        write(response, payResultJson);
    }

    @RequestMapping(value = "/doAuth/{merchantNo}/{orderNo}")
    public String doAuth(@PathVariable(value = "merchantNo") String merchantNo, @PathVariable(value = "orderNo") String orderNo, ModelMap modelMap) {
        logger.info("??????????????????:[{}],?????????:[{}]", merchantNo, orderNo);
        AuthResultVo resultVo = tradePaymentManagerService.userAuth(merchantNo, orderNo);
        modelMap.put("resultVo", resultVo);
        return "auth";
    }

    @RequestMapping(value = "/doProgramAuth/{payKey}/{orderNo}")
    @ResponseBody
    public void doProgramAuth(@PathVariable(value = "payKey") String payKey, @PathVariable(value = "orderNo") String orderNo, HttpServletResponse response) {
        logger.info("?????????payKey:[{}],?????????:[{}]", payKey, orderNo);
        String payResultJson;

        try {
            RpUserPayConfig rpUserPayConfig = userPayConfigService.getByPayKey(payKey);
            if (rpUserPayConfig == null) {
                throw new UserBizException(UserBizException.USER_PAY_CONFIG_ERRPR, "????????????????????????");
            }

            String merchantNo = rpUserPayConfig.getUserNo();// ????????????
            AuthResultVo resultVo = tradePaymentManagerService.userAuth(merchantNo, orderNo);
            payResultJson = JSONObject.toJSONString(resultVo);
        } catch (Exception e) {
            JSONObject resultJson = new JSONObject();
            resultJson.put("errCode", "NO");
            resultJson.put("bankReturnMsg", e.getMessage());
            payResultJson = resultJson.toJSONString();
        }

        logger.debug("?????????--????????????==>{}", payResultJson);
        response.setContentType(CONTENT_TYPE);
        response.setStatus(200);
        write(response, payResultJson);
    }

    @RequestMapping("/orderQuery")
    @ResponseBody
    public String orderQuery(String payKey, String orderNo) {
        logger.info("??????????????????,payKey:[{}],?????????:[{}]", payKey, orderNo);
        RpUserPayConfig rpUserPayConfig = userPayConfigService.getByPayKey(payKey);
        if (rpUserPayConfig == null) {
            throw new UserBizException(UserBizException.USER_PAY_CONFIG_ERRPR, "????????????????????????");
        }

        String merchantNo = rpUserPayConfig.getUserNo();// ????????????
        RpUserBankAuth userBankAuth = userBankAuthService.findByMerchantNoAndPayOrderNo(merchantNo, orderNo);
        JSONObject resultJson = new JSONObject();
        if (userBankAuth == null) {
            resultJson.put("status", "NO");
        } else {
            resultJson.put("status", "YES");
            resultJson.put("returnUrl", AuthConfigUtil.AUTH_ORDER_QUERY_URL + userBankAuth.getMerchantNo() + "/" + userBankAuth.getPayOrderNo());
        }
        return resultJson.toJSONString();
    }


    /**
     * ????????????????????????
     *
     * @param bindingResult
     * @return
     */
    public String getErrorMsg(BindingResult bindingResult) {
        StringBuffer sb = new StringBuffer();
        for (ObjectError objectError : bindingResult.getAllErrors()) {
            sb.append(objectError.getDefaultMessage()).append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

}
