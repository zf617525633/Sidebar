package co.yoyu.sidebar;

import android.app.Activity;
import android.os.Bundle;

import co.yoyu.sidebar.view.SideBar;



public class SidebarLauncher extends Activity {
	/** Called when the activity is first created. */
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            SideBar.showSidebar(this);
            SideBar.showSideBarHandler(this);
            finish();
    }

}
