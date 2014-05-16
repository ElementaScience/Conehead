package org.elementascience.conehead.common;


import junit.framework.Assert;
import org.junit.Test;

public class JobStateTest
{
	@Test
	public void checkNext()
	{
		Assert.assertEquals(JobState.ONDECK, JobState.NEW.next());
		Assert.assertEquals(null, JobState.PUBLISHING.next());
	}

}