package org.openmrs.module.billingui.page.controller;

import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.referenceapplication.ReferenceApplicationWebConstants;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.PageRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ngarivictor on 1/20/2016.
 */
public class QueuePageController {
    public void get(
            PageModel model,
            UiSessionContext sessionContext,
            PageRequest pageRequest,
            UiUtils ui
    ){
        pageRequest.getSession().setAttribute(ReferenceApplicationWebConstants.SESSION_ATTRIBUTE_REDIRECT_URL,ui.thisUrl());
        sessionContext.requireAuthentication();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = sdf.format(new Date());
        model.addAttribute("currentDate", dateStr);
    }

}
