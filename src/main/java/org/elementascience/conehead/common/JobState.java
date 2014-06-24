package org.elementascience.conehead.common;

/**
 * User: dgreen
 * Date: 21/03/2014
 */
public enum JobState
{
	NEW,
	ONDECK,
	UNPACKING,
	PREPARING,
	INGESTING,
	STAGING,
	FINAL
	{
		@Override
		public JobState next()
		{
			return null; // see below for options for this line
		}
	};

	public JobState next()
	{
		// No bounds checking required here, because the last instance overrides
		return values()[ordinal() + 1];
	}
}
