package io.mosip.kernel.pridgenerator.test.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.idgenerator.prid.impl.PridGeneratorImpl;
import io.mosip.kernel.idgenerator.prid.util.PridFilterUtils;

@RunWith(MockitoJUnitRunner.class)
public class PridGeneratorImplTest {

	@InjectMocks
	private PridGeneratorImpl pridGenerator;

	@Mock
	private PridFilterUtils pridFilterUtils;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(pridGenerator, "pridLength", 14);
		when(pridFilterUtils.isValidId(anyString())).thenReturn(true);
	}

	@Test
	public void generateIdReturnsFilteredValueOfConfiguredLength() {
		String prid = pridGenerator.generateId();
		assertNotNull(prid);
		assertEquals(14, prid.length());
	}
}
