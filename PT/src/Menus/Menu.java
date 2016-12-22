package Menus;

import java.util.ArrayList;

public interface Menu {
	
	public void refreshVisibleOptions();
	public int getNumOptions();
	public ArrayList<String> getVisibleOptions();
	public void select(short index);
}
