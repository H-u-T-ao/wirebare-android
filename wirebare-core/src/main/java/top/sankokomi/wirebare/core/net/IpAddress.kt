package top.sankokomi.wirebare.core.net

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import top.sankokomi.wirebare.core.util.convertIpv4ToInt
import top.sankokomi.wirebare.core.util.convertIpv4ToString
import top.sankokomi.wirebare.core.util.convertIpv6ToInt
import top.sankokomi.wirebare.core.util.convertIpv6ToString

/**
 * ip 地址
 * */
class IpAddress : Parcelable {

    val ipVersion: IpVersion

    val intIpv4: Int

    val intIpv6: IntIpv6

    val stringIp: String

    constructor(ipv4Address: Int) {
        this.ipVersion = IpVersion.IPv4
        this.intIpv4 = ipv4Address
        this.stringIp = intIpv4.convertIpv4ToString
        this.intIpv6 = IntIpv6(0L, 0L)
    }

    constructor(ipv6Address: IntIpv6) {
        this.ipVersion = IpVersion.IPv6
        this.intIpv6 = ipv6Address
        this.stringIp = intIpv6.convertIpv6ToString
        this.intIpv4 = 0
    }

    constructor(address: String, ipVersion: IpVersion) {
        this.ipVersion = ipVersion
        when (ipVersion) {
            IpVersion.IPv4 -> {
                this.intIpv4 = address.convertIpv4ToInt
                this.stringIp = address
                this.intIpv6 = IntIpv6(0L, 0L)
            }

            IpVersion.IPv6 -> {
                this.intIpv6 = address.convertIpv6ToInt
                this.stringIp = address
                this.intIpv4 = 0
            }
        }
    }

    override fun toString(): String = stringIp

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IpAddress
        return stringIp == other.stringIp
    }

    override fun hashCode(): Int {
        return stringIp.hashCode()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(intIpv4)
    }

    private constructor(parcel: Parcel) : this(parcel.readInt())

    companion object CREATOR : Creator<IpAddress> {
        override fun createFromParcel(parcel: Parcel): IpAddress {
            return IpAddress(parcel)
        }

        override fun newArray(size: Int): Array<IpAddress?> {
            return arrayOfNulls(size)
        }
    }

}