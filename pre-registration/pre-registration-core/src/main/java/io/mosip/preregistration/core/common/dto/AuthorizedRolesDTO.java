package io.mosip.preregistration.core.common.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.prereg")
@Data
public class AuthorizedRolesDTO {
	private List<String> putapplications;

	private List<String> postapplications;

	private List<String> getapplications;

	private List<String> putapplicationsstatus;

	private List<String> getapplicationsall;

	private List<String> getapplicationsstatus;

	private List<String> deleteapplications;

	private List<String> preregdemodeleteapplications;

	private List<String> postapplicationsupdatedtime;

	private List<String> getapplicationsconfig;

	private List<String> getapplicationsinfo;

	private List<String> postlogaudit;

	private List<String> postdocumentspreregistrationid;

	private List<String> putdocumentspreregistrationid;

	private List<String> getdocumentspreregistrationid;

	private List<String> getdocumentsdocumentid;

	private List<String> deletedocumentsdocumentid;

	private List<String> deletedocumentspreregistrationid;

	private List<String> putdocumentsdocumentid;

	private List<String> postqrcodegenerate;

	private List<String> getrefreshconfig;

	private List<String> postnotificationnotify;

	private List<String> postnotification;

	private List<String> posttransliterationtransliterate;

	private List<String> getuispeclatest;

	private List<String> getuispecall;

	private List<String> postpreregsync;

	private List<String> getsyncpreregistrationid;

	private List<String> getsyncpreregistrationidmachineid;

	private List<String> postsyncconsumedpreregids;

	private List<String> getslotsavailablity;

	private List<String> getappointmentdetailspreregid;

	private List<String> postappointmentpregid;

	private List<String> deleteappointmentpreregid;

	private List<String> cancelappointmentpreregid;

	private List<String> postappointmentmulti;

	private List<String> updateapplicationstatusappid;

	private List<String> getapplicationdetailsappid;

	private List<String> getappointmentavailability;

	private List<String> postappointmentpreregistrationid;

	private List<String> postappointment;

	private List<String> getappointmentpreregistrationid;

	private List<String> putappointmentpreregistrationid;

	private List<String> putbatchappointmentpreregistrationid;

	private List<String> deleteappointment;

	private List<String> getappointmentpreregistrationidregistrationcenterid;

	private List<String> getappointmentregistrationcenterid;
}