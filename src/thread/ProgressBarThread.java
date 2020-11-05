package thread;

//this is a skeleton of a thread that updates the value of
//the progress bar, making it look like it is charging.
//For this to work, this needs a method in the controller
//that receives the values made here and updates the value 
//of the progress bar. This also depends on the model sending 
//the value of the time or whatever measure is going to be used
//to calculate the thing on the thread

import ui.Controller;

public class ProgressBarThread extends Thread {
	
	private Controller controller;

	public ProgressBarThread(Controller controller) {
		this.controller = controller;
		setDaemon(true);
	}
	
	@Override
	public void run() {
		double time = 0.01;
		while(time<=1) {
			time+= 0.01;
			controller.updateProgressBar(time);
			try {
				sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


}