package hecdssvue.cdec.plugin;

import junit.framework.TestCase;

public class CDECSensorDefsTest extends TestCase {
	public void testCacheSensorDefs() throws Exception{
		CDECSensorDefs defs = CDECSensorDefs.get();
		assertTrue(defs.getDefs().size() > 0);
	}
}
