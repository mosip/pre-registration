package io.mosip.preregistration.core.common.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.prereg")
@Getter
@Setter
public class AuthorizedRolesDTO {

	
    private List<String> putapplications;
    
    private List<String> postapplications;

    private List<String> getapplications;
	
    private List<String> putapplicationsstatus;
	
    private List<String> getapplicationsall;
	
    private List<String> getapplicationsstatus;
	
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
    
}
