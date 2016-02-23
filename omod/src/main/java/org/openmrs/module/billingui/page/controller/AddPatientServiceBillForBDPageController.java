package org.openmrs.module.billingui.page.controller;


import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.BillingService;
import org.openmrs.module.hospitalcore.model.BillableService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Stanslaus Odhiambo
 *         Created on 2/22/2016.
 */
public class AddPatientServiceBillForBDPageController {

    public String get(PageModel pageModel, @RequestParam("patientId") Integer patientId,
                      @RequestParam(value = "comment", required = false) String comment,
                      @RequestParam(value = "billType", required = false) String billType,
                      @RequestParam(value = "encounterId", required = false) Integer encounterId,
                      @RequestParam(value = "typeOfPatient", required = false) String typeOfPatient,
                      UiUtils uiUtils) {
        BillingService billingService = Context.getService(BillingService.class);
        List<BillableService> services = billingService.getAllServices();
        Map<Integer, BillableService> mapServices = new HashMap<Integer, BillableService>();
        for (BillableService ser : services) {
            mapServices.put(ser.getConceptId(), ser);
        }
        Integer conceptId = Integer.valueOf(Context.getAdministrationService().getGlobalProperty(
                "billing.rootServiceConceptId"));
        Concept concept = Context.getConceptService().getConcept(conceptId);
        pageModel.addAttribute("tabs", billingService.traversTab(concept, mapServices, 1));

        pageModel.addAttribute("patientId", patientId);
        Map<String,Object> params =new HashMap<String, Object>();

        params.put("patientId",patientId);
        if(StringUtils.isNotBlank(comment)){
            params.put("comment",comment);
        }
        if(StringUtils.isNotBlank(billType)){
            params.put("billType",billType);
        }
        if(encounterId>0){
            params.put("encounterId",encounterId);
        }

        if (StringUtils.isNotBlank(typeOfPatient)) {
            return "redirect:" + uiUtils.pageLink("billingui", "billableServiceBillAddForIndoorPatient",params);
        } else {
            return "redirect:" + uiUtils.pageLink("billingui", "billableServiceBillAdd",params);
        }
    }
}
