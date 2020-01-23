/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjob.exception.util;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.batchjob.exception.NoPreIdAvailableException;
import io.mosip.preregistration.batchjob.exception.NoValidPreIdFoundException;
import io.mosip.preregistration.batchjob.exception.RestCallException;
import io.mosip.preregistration.core.exception.TableNotAccessibleException;

/**
 * This class is used to catch the exceptions that occur in Batch Service
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
public class BatchServiceExceptionCatcher {
	
	/**
	 * Method to handle the respective exceptions
	 * 
	 * @param ex
	 *            pass the exception
	 */
	public void handle(Exception ex) {
		 if (ex instanceof NoPreIdAvailableException) {
			throw new NoPreIdAvailableException(((NoPreIdAvailableException) ex).getErrorCode(),
					((NoPreIdAvailableException) ex).getErrorText());
		}
		else if (ex instanceof DataAccessLayerException) {
			throw new TableNotAccessibleException(((DataAccessLayerException) ex).getErrorCode(),
					((DataAccessLayerException) ex).getErrorText());
		}
		else if (ex instanceof NoValidPreIdFoundException) {
			throw new NoValidPreIdFoundException(((NoValidPreIdFoundException) ex).getErrorCode(),
					((NoValidPreIdFoundException) ex).getErrorText());
		}
		else if (ex instanceof TableNotAccessibleException) {
			throw new TableNotAccessibleException(((TableNotAccessibleException) ex).getErrorCode(),
					((TableNotAccessibleException) ex).getErrorText());
		}
		else if (ex instanceof RestCallException) {
			throw new RestCallException(((RestCallException) ex).getErrorCode(),
					((RestCallException) ex).getErrorText());
		}
	}

}
