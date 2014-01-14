package com.travelbook.holder;


import android.os.Parcel;
import android.os.Parcelable;

public class ThumbImageInfo implements Parcelable
{
  private String id;
  private String data;
  private boolean checkedState;
  
  public ThumbImageInfo(){
	  
  }
  public ThumbImageInfo(Parcel in){
	  readFromParcel(in);
  }
  
  
  public String getId()
  {
    return id;
  }
  public void setId(String id)
  {
    this.id = id;
  }
  public String getData()
  {
    return data;
  }
  public void setData(String data)
  {
    this.data = data;
  }
  public boolean getCheckedState()
  {
    return checkedState;
  }
  public void setCheckedState(boolean checkedState)
  {
    this.checkedState = checkedState;
  }
  
  
  
  
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(id);
		dest.writeString(data);
		
	}
	private void readFromParcel(Parcel in){
		id=in.readString();
		data=in.readString();
	}
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public ThumbImageInfo createFromParcel(Parcel in) {
			// TODO Auto-generated method stub
			return new ThumbImageInfo(in);
		}

		@Override
		public ThumbImageInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ThumbImageInfo[size];
		}
	};
}
