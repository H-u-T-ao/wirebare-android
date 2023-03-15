package org.github.kokomi.wirebare.net

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import org.github.kokomi.wirebare.util.convertIpInt
import org.github.kokomi.wirebare.util.convertIpString

/**
 * ipv4 地址
 * */
class Ipv4Address : Parcelable {

    val int: Int

    val string: String

    constructor(address: Int) {
        this.int = address
        this.string = int.convertIpString
    }

    constructor(address: String) {
        this.int = address.convertIpInt
        this.string = int.convertIpString
    }

    override fun toString(): String = string

    override fun describeContents(): Int = 0

    override fun equals(other: Any?): Boolean {
        if (other !is Ipv4Address) return false
        if (this === other) return true
        return this.int == other.int
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(int)
    }

    override fun hashCode(): Int {
        var result = int
        result = 31 * result + string.hashCode()
        return result
    }

    private constructor(parcel: Parcel) : this(parcel.readInt())

    companion object CREATOR : Creator<Ipv4Address> {
        override fun createFromParcel(parcel: Parcel): Ipv4Address {
            return Ipv4Address(parcel)
        }

        override fun newArray(size: Int): Array<Ipv4Address?> {
            return arrayOfNulls(size)
        }
    }

}