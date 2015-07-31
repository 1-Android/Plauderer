package de.devacon.xorando.plauderei;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by @Martin@ on 31.07.2015 09:52.
 */
public class Person implements Parcelable {
    public Person(String internalName, Gender gender, String firstName, String lastName, int resIcon, File picture) {
        name = internalName;
        this.gender = gender;
        this.firstName = firstName;
        this.lastName = lastName;
        this.resIcon = resIcon;
        this.picture = picture;
    }
    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);

        dest.writeByte((byte) (gender == Gender.MALE ? 1 : gender == Gender.FEMALE ? 2 : 0));
        dest.writeString(firstName);
        dest.writeString(lastName);

        dest.writeString(picture != null? picture.getAbsolutePath():"");
    }
    private Person(Parcel in){
        name = in.readString();
        byte b = in.readByte();
        gender = b == 1 ? Gender.MALE : b == 2 ? Gender.FEMALE : Gender.UNKNOWN;
        firstName = in.readString();
        lastName = in.readString();
        String fileName = in.readString();
        if(fileName.isEmpty()){
            picture = null;
        }
        else
            picture = new File(fileName);
    }
    static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>(){
        public Person createFromParcel(Parcel in) {
                return new Person(in);
        }

        /**
         * Create a new array of the Parcelable class.
         *
         * @param size Size of the array.
         * @return Returns an array of the Parcelable class, with every entry
         * initialized to null.
         */
        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
    enum Gender {
        UNKNOWN ,
        MALE ,
        FEMALE,
    }
    public int resIcon = 0;
    public String firstName = "";
    public String lastName = "";
    Gender gender = Gender.UNKNOWN;
    public File picture = null;
    public String name = null;
    Person(String internalName) {
        name = internalName;
    }
    Person(){

    }
    @Override
    public String toString() {
        String ret = "";
        if(firstName.isEmpty()) {
            if(gender == Gender.FEMALE) {
                ret += "Frau ";
            }
            else if(gender == Gender.MALE) {
                ret += "Herr ";
            }
            if(lastName.isEmpty()){
                ret += "Unbekannt";
            }
            else
                ret += lastName;
        }
        else {
            ret += firstName;
            if(!lastName.isEmpty()){
                ret += " " + lastName;
            }
        }
        return ret;
    }

}
