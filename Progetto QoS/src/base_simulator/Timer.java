/**************************
 * @author Franco
 **************************/
 
 
package base_simulator;



public class Timer implements Cloneable{
	private double current_Time;
	
	public Timer()
	{
		this.current_Time = 0;
	}

	public double getCurrent_Time() {
		return current_Time;
	}

	public void setCurrent_Time(double current_Time) {
		this.current_Time = current_Time;
	}
	
	public void shifta(double ms)
	{
		this.current_Time = this.current_Time+ms;
	}
	
	public Object clone(){
		try{
			return super.clone();
		}catch(CloneNotSupportedException e){return null;}
	}

}
