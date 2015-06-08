package hecdssvue.cdec.plugin;

import hec.dssgui.HecDssVue;

public class TestCDECPlugin {
	public static void main(String[] args) throws Exception{
		HecDssVue.main(new String[]{"../hecdssvue.cdec.plugin/test.dss"});
		//call this plugin manually
		CDECPlugin.main(new Object[]{HecDssVue.getMainWindow()});
	}
}
