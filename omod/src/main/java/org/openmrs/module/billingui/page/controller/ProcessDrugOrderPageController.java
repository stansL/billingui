package org.openmrs.module.billingui.page.controller;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.hospitalcore.HospitalCoreService;
import org.openmrs.module.hospitalcore.model.*;
import org.openmrs.module.hospitalcore.util.ActionValue;
import org.openmrs.module.inventory.InventoryService;
import org.openmrs.module.inventory.util.DateUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Stanslaus Odhiambo
 *         Created on 4/17/2016.
 */
public class ProcessDrugOrderPageController {
    public void get(PageModel model,
                    @RequestParam("orderId") Integer orderId, PageModel pageModel,
                    UiUtils uiUtils) {
        InventoryService inventoryService = Context.getService(InventoryService.class);
        List<Role> role = new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());

        InventoryStoreRoleRelation srl = null;
        Role rl = null;
        for (Role r : role) {
            if (inventoryService.getStoreRoleByName(r.toString()) != null) {
                srl = inventoryService.getStoreRoleByName(r.toString());
                rl = r;
            }
        }
        InventoryStore store = null;
        if (srl != null) {
            store = inventoryService.getStoreById(srl.getStoreid());

        }
        List<InventoryStoreDrugPatientDetail> listDrugIssue = inventoryService
                .listStoreDrugPatientDetail(orderId);
        InventoryStoreDrugPatient inventoryStoreDrugPatient = new InventoryStoreDrugPatient();
        if (inventoryStoreDrugPatient != null && listDrugIssue != null && listDrugIssue.size() > 0) {


            InventoryStoreDrugTransaction transaction = new InventoryStoreDrugTransaction();
            transaction.setDescription("ISSUE DRUG TO PATIENT " + DateUtils.getDDMMYYYY());
            transaction.setStore(store);
            transaction.setTypeTransaction(ActionValue.TRANSACTION[1]);
            transaction.setCreatedBy(Context.getAuthenticatedUser().getGivenName());

            transaction = inventoryService.saveStoreDrugTransaction(transaction);
            for (InventoryStoreDrugPatientDetail pDetail : listDrugIssue) {
                Date date1 = new Date();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Integer totalQuantity = inventoryService.sumCurrentQuantityDrugOfStore(store.getId(), pDetail
                                .getTransactionDetail().getDrug().getId(),
                        pDetail.getTransactionDetail().getFormulation().getId());
                int t = totalQuantity;

                Integer receipt = pDetail.getStoreDrugPatient().getId();
                model.addAttribute("receiptid", receipt);

                InventoryStoreDrugTransactionDetail inventoryStoreDrugTransactionDetail = inventoryService
                        .getStoreDrugTransactionDetailById(pDetail.getTransactionDetail().getParent().getId());

                InventoryStoreDrugTransactionDetail drugTransactionDetail = inventoryService.getStoreDrugTransactionDetailById(inventoryStoreDrugTransactionDetail.getId());

                inventoryStoreDrugTransactionDetail.setCurrentQuantity(drugTransactionDetail.getCurrentQuantity());
                Integer flags = pDetail.getTransactionDetail().getFlag();
                model.addAttribute("flag", flags);
                inventoryService.saveStoreDrugTransactionDetail(inventoryStoreDrugTransactionDetail);
                // save transactiondetail first
                InventoryStoreDrugTransactionDetail transDetail = new InventoryStoreDrugTransactionDetail();
                transDetail.setTransaction(transaction);
                transDetail.setCurrentQuantity(0);
                transDetail.setIssueQuantity(pDetail.getQuantity());
                transDetail.setOpeningBalance(totalQuantity);
                transDetail.setClosingBalance(t);
                transDetail.setQuantity(0);
                transDetail.setVAT(pDetail.getTransactionDetail().getVAT());
                transDetail.setCostToPatient(pDetail.getTransactionDetail().getCostToPatient());
                transDetail.setUnitPrice(pDetail.getTransactionDetail().getUnitPrice());
                transDetail.setDrug(pDetail.getTransactionDetail().getDrug());
                transDetail.setFormulation(pDetail.getTransactionDetail().getFormulation());
                transDetail.setBatchNo(pDetail.getTransactionDetail().getBatchNo());
                transDetail.setCompanyName(pDetail.getTransactionDetail().getCompanyName());
                transDetail.setDateManufacture(pDetail.getTransactionDetail().getDateManufacture());
                transDetail.setDateExpiry(pDetail.getTransactionDetail().getDateExpiry());
                transDetail.setReceiptDate(pDetail.getTransactionDetail().getReceiptDate());
                transDetail.setCreatedOn(date1);
                transDetail.setReorderPoint(pDetail.getTransactionDetail().getDrug().getReorderQty());
                transDetail.setAttribute(pDetail.getTransactionDetail().getDrug().getAttributeName());
                transDetail.setFrequency(pDetail.getTransactionDetail().getFrequency());
                transDetail.setNoOfDays(pDetail.getTransactionDetail().getNoOfDays());
                transDetail.setComments(pDetail.getTransactionDetail().getComments());
                transDetail.setFlag(1);

                BigDecimal moneyUnitPrice = pDetail.getTransactionDetail().getCostToPatient().multiply(new BigDecimal(pDetail.getQuantity()));
                transDetail.setTotalPrice(moneyUnitPrice);
                transDetail.setParent(pDetail.getTransactionDetail());
                transDetail = inventoryService.saveStoreDrugTransactionDetail(transDetail);

            }

        }

        List<SimpleObject> dispensedDrugs = SimpleObject.fromCollection(listDrugIssue, uiUtils, "quantity", "transactionDetail.costToPatient", "transactionDetail.drug.name",
                "transactionDetail.formulation.name", "transactionDetail.formulation.dozage", "transactionDetail.frequency.name", "transactionDetail.noOfDays",
                "transactionDetail.comments", "transactionDetail.dateExpiry");
        model.addAttribute("listDrugIssue", SimpleObject.create("listDrugIssue", dispensedDrugs).toJson());
        if (CollectionUtils.isNotEmpty(listDrugIssue)) {
            model.addAttribute("issueDrugPatient", listDrugIssue.get(0).getStoreDrugPatient());
            model.addAttribute("date", listDrugIssue.get(0).getStoreDrugPatient().getCreatedOn());

            int age = listDrugIssue.get(0).getStoreDrugPatient().getPatient().getAge();
            if (age < 1) {
                model.addAttribute("age", "<1");
            } else {
                model.addAttribute("age", age);
            }
            //TODO starts here

            PatientIdentifier pi = listDrugIssue.get(0).getStoreDrugPatient().getPatient().getPatientIdentifier();
            int patientId = pi.getPatient().getPatientId();
            Date issueDate = listDrugIssue.get(0).getStoreDrugPatient().getCreatedOn();
            Encounter encounterId = listDrugIssue.get(0).getTransactionDetail().getEncounter();
            List<OpdDrugOrder> listOfNotDispensedOrder = new ArrayList<OpdDrugOrder>();
            if (encounterId != null) {
                listOfNotDispensedOrder = inventoryService.listOfNotDispensedOrder(patientId, issueDate, encounterId);

            }
            List<SimpleObject> notDispensed = SimpleObject.fromCollection(listOfNotDispensedOrder, uiUtils, "inventoryDrug.name",
                    "inventoryDrugFormulation.name", "inventoryDrugFormulation.dozage", "frequency.name", "noOfDays", "comments");
            model.addAttribute("listOfNotDispensedOrder", SimpleObject.create("listOfNotDispensedOrder", notDispensed).toJson());
            //TODO ends here


            model.addAttribute("identifier", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getPatientIdentifier());
            model.addAttribute("givenName", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getGivenName());
            model.addAttribute("familyName", listDrugIssue.get(0).getStoreDrugPatient().getPatient().getFamilyName());
            if (listDrugIssue.get(0).getStoreDrugPatient().getPatient().getMiddleName() != null) {
                model.addAttribute("middleName", listDrugIssue.get(0)
                        .getStoreDrugPatient().getPatient().getMiddleName());
            } else {
                model.addAttribute("middleName", " ");
            }
            if (listDrugIssue.get(0)
                    .getStoreDrugPatient().getPatient().getGender().equals("M")) {
                model.addAttribute("gender", "Male");
            }
            if (listDrugIssue.get(0)
                    .getStoreDrugPatient().getPatient().getGender().equals("F")) {
                model.addAttribute("gender", "Female");
            }

            model.addAttribute("processor", listDrugIssue.get(0).getStoreDrugPatient().getCreatedBy());
            model.addAttribute("cashier", Context.getAuthenticatedUser().getPersonName());

            HospitalCoreService hcs = Context.getService(HospitalCoreService.class);
            List<PersonAttribute> pas = hcs.getPersonAttributes(listDrugIssue.get(0)
                    .getStoreDrugPatient().getPatient().getId());
            for (PersonAttribute pa : pas) {
                PersonAttributeType attributeType = pa.getAttributeType();
                PersonAttributeType personAttributePCT = hcs.getPersonAttributeTypeByName("Paying Category Type");
                PersonAttributeType personAttributeNPCT = hcs.getPersonAttributeTypeByName("Non-Paying Category Type");
                PersonAttributeType personAttributeSSCT = hcs.getPersonAttributeTypeByName("Special Scheme Category Type");
                if (attributeType.getPersonAttributeTypeId() == personAttributePCT.getPersonAttributeTypeId()) {
                    model.addAttribute("paymentSubCategory", pa.getValue());
                } else if (attributeType.getPersonAttributeTypeId() == personAttributeNPCT.getPersonAttributeTypeId()) {
                    model.addAttribute("paymentSubCategory", pa.getValue());
                } else if (attributeType.getPersonAttributeTypeId() == personAttributeSSCT.getPersonAttributeTypeId()) {
                    model.addAttribute("paymentSubCategory", pa.getValue());
                }
            }
        }
    }

    public String post(@RequestParam(value = "receiptid", required = false) Integer receiptid, HttpServletRequest request,
                       @RequestParam(value = "flag", required = false) Integer flag, PageModel pageModel, UiUtils uiUtils) {
        String drugOrder = request.getParameter("drugOrder");

        InventoryService inventoryService = (InventoryService) Context
                .getService(InventoryService.class);
        //InventoryStore store =  inventoryService.getStoreByCollectionRole(new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles()));
        List<Role> role = new ArrayList<Role>(Context.getAuthenticatedUser().getAllRoles());

        InventoryStoreRoleRelation srl = null;
        Role rl = null;
        for (Role r : role) {
            if (inventoryService.getStoreRoleByName(r.toString()) != null) {
                srl = inventoryService.getStoreRoleByName(r.toString());
                rl = r;
            }
        }
        InventoryStore store = null;
        if (srl != null) {
            store = inventoryService.getStoreById(srl.getStoreid());

        }
        List<InventoryStoreDrugPatientDetail> listDrugIssue = inventoryService
                .listStoreDrugPatientDetail(receiptid);
        InventoryStoreDrugPatient inventoryStoreDrugPatient = new InventoryStoreDrugPatient();




        return "";
    }
}
