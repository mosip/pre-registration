package io.mosip.preregistration.core.common.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.prereg")
@Data
/**
 * Data transfer object carrying authorized roles across Pre-Registration APIs.
 */
public class AuthorizedRolesDTO {
	/**
	 * putapplications used by this Pre-Registration type.
	 */
	private List<String> putapplications;

	/**
	 * postapplications used by this Pre-Registration type.
	 */
	private List<String> postapplications;

	/**
	 * getapplications used by this Pre-Registration type.
	 */
	private List<String> getapplications;

	/**
	 * putapplicationsstatus used by this Pre-Registration type.
	 */
	private List<String> putapplicationsstatus;

	/**
	 * getapplicationsall used by this Pre-Registration type.
	 */
	private List<String> getapplicationsall;

	/**
	 * getapplicationsstatus used by this Pre-Registration type.
	 */
	private List<String> getapplicationsstatus;

	/**
	 * deleteapplications used by this Pre-Registration type.
	 */
	private List<String> deleteapplications;

	/**
	 * preregdemodeleteapplications used by this Pre-Registration type.
	 */
	private List<String> preregdemodeleteapplications;

	/**
	 * postapplicationsupdatedtime used by this Pre-Registration type.
	 */
	private List<String> postapplicationsupdatedtime;

	/**
	 * getapplicationsconfig used by this Pre-Registration type.
	 */
	private List<String> getapplicationsconfig;

	/**
	 * getapplicationsinfo used by this Pre-Registration type.
	 */
	private List<String> getapplicationsinfo;

	/**
	 * postlogaudit used by this Pre-Registration type.
	 */
	private List<String> postlogaudit;

	/**
	 * postdocumentspreregistrationid used by this Pre-Registration type.
	 */
	private List<String> postdocumentspreregistrationid;

	/**
	 * putdocumentspreregistrationid used by this Pre-Registration type.
	 */
	private List<String> putdocumentspreregistrationid;

	/**
	 * getdocumentspreregistrationid used by this Pre-Registration type.
	 */
	private List<String> getdocumentspreregistrationid;

	/**
	 * getdocumentsdocumentid used by this Pre-Registration type.
	 */
	private List<String> getdocumentsdocumentid;

	/**
	 * deletedocumentsdocumentid used by this Pre-Registration type.
	 */
	private List<String> deletedocumentsdocumentid;

	/**
	 * deletedocumentspreregistrationid used by this Pre-Registration type.
	 */
	private List<String> deletedocumentspreregistrationid;

	/**
	 * putdocumentsdocumentid used by this Pre-Registration type.
	 */
	private List<String> putdocumentsdocumentid;

	/**
	 * postqrcodegenerate used by this Pre-Registration type.
	 */
	private List<String> postqrcodegenerate;

	/**
	 * getrefreshconfig used by this Pre-Registration type.
	 */
	private List<String> getrefreshconfig;

	/**
	 * postnotificationnotify used by this Pre-Registration type.
	 */
	private List<String> postnotificationnotify;

	/**
	 * postnotification used by this Pre-Registration type.
	 */
	private List<String> postnotification;

	/**
	 * posttransliterationtransliterate used by this Pre-Registration type.
	 */
	private List<String> posttransliterationtransliterate;

	/**
	 * getuispeclatest used by this Pre-Registration type.
	 */
	private List<String> getuispeclatest;

	/**
	 * getuispecall used by this Pre-Registration type.
	 */
	private List<String> getuispecall;

	/**
	 * postpreregsync used by this Pre-Registration type.
	 */
	private List<String> postpreregsync;

	/**
	 * getsyncpreregistrationid used by this Pre-Registration type.
	 */
	private List<String> getsyncpreregistrationid;

	/**
	 * getsyncpreregistrationidmachineid used by this Pre-Registration type.
	 */
	private List<String> getsyncpreregistrationidmachineid;

	/**
	 * postsyncconsumedpreregids used by this Pre-Registration type.
	 */
	private List<String> postsyncconsumedpreregids;

	/**
	 * getslotsavailablity used by this Pre-Registration type.
	 */
	private List<String> getslotsavailablity;

	/**
	 * getappointmentdetailspreregid used by this Pre-Registration type.
	 */
	private List<String> getappointmentdetailspreregid;

	/**
	 * postappointmentpregid used by this Pre-Registration type.
	 */
	private List<String> postappointmentpregid;

	/**
	 * deleteappointmentpreregid used by this Pre-Registration type.
	 */
	private List<String> deleteappointmentpreregid;

	/**
	 * cancelappointmentpreregid used by this Pre-Registration type.
	 */
	private List<String> cancelappointmentpreregid;

	/**
	 * postappointmentmulti used by this Pre-Registration type.
	 */
	private List<String> postappointmentmulti;

	/**
	 * updateapplicationstatusappid used by this Pre-Registration type.
	 */
	private List<String> updateapplicationstatusappid;

	/**
	 * getapplicationdetailsappid used by this Pre-Registration type.
	 */
	private List<String> getapplicationdetailsappid;

	/**
	 * getappointmentavailability used by this Pre-Registration type.
	 */
	private List<String> getappointmentavailability;

	/**
	 * postappointmentpreregistrationid used by this Pre-Registration type.
	 */
	private List<String> postappointmentpreregistrationid;

	/**
	 * postappointment used by this Pre-Registration type.
	 */
	private List<String> postappointment;

	/**
	 * getappointmentpreregistrationid used by this Pre-Registration type.
	 */
	private List<String> getappointmentpreregistrationid;

	/**
	 * putappointmentpreregistrationid used by this Pre-Registration type.
	 */
	private List<String> putappointmentpreregistrationid;

	/**
	 * putbatchappointmentpreregistrationid used by this Pre-Registration type.
	 */
	private List<String> putbatchappointmentpreregistrationid;

	/**
	 * deleteappointment used by this Pre-Registration type.
	 */
	private List<String> deleteappointment;

	/**
	 * getappointmentpreregistrationidregistrationcenterid used by this Pre-Registration type.
	 */
	private List<String> getappointmentpreregistrationidregistrationcenterid;

	/**
	 * getappointmentregistrationcenterid used by this Pre-Registration type.
	 */
	private List<String> getappointmentregistrationcenterid;
}
