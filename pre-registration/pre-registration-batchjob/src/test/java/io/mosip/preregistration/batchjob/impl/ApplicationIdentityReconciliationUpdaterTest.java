package io.mosip.preregistration.batchjob.impl;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.mosip.preregistration.batchjob.repository.utils.BatchJpaRepositoryImpl;

@RunWith(JUnit4.class)
public class ApplicationIdentityReconciliationUpdaterTest {

	@InjectMocks
	private ApplicationIdentityReconciliationUpdater updater;

	@Mock
	private BatchJpaRepositoryImpl batchJpaRepositoryImpl;

	@Mock
	private IdentityReconciliationTxHelper reconciliationTxHelper;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void reconcilesEachStuckIdThenStopsWhenEmpty() {
		// First batch has two ids (both reconciled -> progress), then the predicate returns none.
		when(batchJpaRepositoryImpl.getPreRegIdsWithRawIdentifier(anyInt())).thenReturn(List.of("p1", "p2"),
				Collections.emptyList());
		when(reconciliationTxHelper.reconcileOne("p1")).thenReturn(true);
		when(reconciliationTxHelper.reconcileOne("p2")).thenReturn(true);

		updater.reconcileRawIdentityRecords();

		verify(reconciliationTxHelper).reconcileOne("p1");
		verify(reconciliationTxHelper).reconcileOne("p2");
		verify(batchJpaRepositoryImpl, times(2)).getPreRegIdsWithRawIdentifier(anyInt());
	}

	@Test
	public void stopsWhenABatchMakesNoProgress() {
		// The same id keeps matching (resolution never succeeds); the loop must stop after one
		// zero-progress batch rather than re-querying forever.
		when(batchJpaRepositoryImpl.getPreRegIdsWithRawIdentifier(anyInt())).thenReturn(List.of("p1"));
		when(reconciliationTxHelper.reconcileOne("p1")).thenReturn(false);

		updater.reconcileRawIdentityRecords();

		verify(reconciliationTxHelper, times(1)).reconcileOne("p1");
		verify(batchJpaRepositoryImpl, times(1)).getPreRegIdsWithRawIdentifier(anyInt());
	}

	@Test
	public void doesNothingWhenNoCandidates() {
		when(batchJpaRepositoryImpl.getPreRegIdsWithRawIdentifier(anyInt())).thenReturn(Collections.emptyList());

		updater.reconcileRawIdentityRecords();

		verify(reconciliationTxHelper, never()).reconcileOne(anyString());
		verify(batchJpaRepositoryImpl, times(1)).getPreRegIdsWithRawIdentifier(anyInt());
	}

	@Test
	public void continuesPastAPerRecordException() {
		when(batchJpaRepositoryImpl.getPreRegIdsWithRawIdentifier(anyInt())).thenReturn(List.of("p1", "p2"),
				Collections.emptyList());
		when(reconciliationTxHelper.reconcileOne("p1")).thenThrow(new RuntimeException("boom"));
		when(reconciliationTxHelper.reconcileOne("p2")).thenReturn(true);

		updater.reconcileRawIdentityRecords();

		verify(reconciliationTxHelper).reconcileOne("p1");
		verify(reconciliationTxHelper).reconcileOne("p2");
	}
}
