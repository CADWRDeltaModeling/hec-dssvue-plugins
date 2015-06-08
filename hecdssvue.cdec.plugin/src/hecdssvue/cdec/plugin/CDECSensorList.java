package hecdssvue.cdec.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of CDEC stations and sensors that are to be marked as selected so that they can be used in group operations such as download/uploading data for those.
 * @author psandhu
 *
 */
public class CDECSensorList {
	private ArrayList<CDECSensor> list;
	public CDECSensorList(){
		list = new ArrayList<CDECSensor>();
	}
	
	public void setList(List<CDECSensor> list){
		this.list.clear();
		this.list.addAll(list);
	}
	
	public List<CDECSensor> getList(){
		return list;
	}
	
}
