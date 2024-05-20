package top.sankokomi.wirebare.core.ssl

import android.os.Build
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.bc.BcX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.ContentSigner
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import top.sankokomi.wirebare.core.util.closeSafely
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Date
import java.util.Random

object CertificateFactory {

    fun generateServer(
        commonName: String?,
        jks: JKS,
        certificate: Certificate,
        privateKey: PrivateKey
    ): KeyStore? {
        val keyPair: KeyPair = generateKeyPair()
        val issuer: X500Name = X509CertificateHolder(certificate.encoded).subject
        val serial = BigInteger.valueOf(randomSerial())
        val name = X500NameBuilder(BCStyle.INSTANCE)
        name.addRDN(BCStyle.CN, commonName)
        name.addRDN(BCStyle.O, jks.organization)
        name.addRDN(BCStyle.OU, jks.organizationUnit)
        val subject: X500Name = name.build()
        val builder: X509v3CertificateBuilder = JcaX509v3CertificateBuilder(
            issuer,
            serial,
            Date(System.currentTimeMillis() - 86400000L),
            Date(System.currentTimeMillis() + 86400000L),
            subject,
            keyPair.public
        )
        builder.addExtension(
            Extension.subjectKeyIdentifier, false,
            createSubjectKeyIdentifier(
                keyPair.public
            )
        )
        builder.addExtension(
            Extension.basicConstraints, false,
            BasicConstraints(false)
        )
        builder.addExtension(
            Extension.subjectAlternativeName, false,
            DERSequence(GeneralName(GeneralName.dNSName, commonName))
        )
        val cert: X509Certificate = signCertificate(
            builder,
            privateKey
        )
        cert.checkValidity(Date())
        cert.verify(certificate.publicKey)
        val result = KeyStore.getInstance(KeyStore.getDefaultType())
        result.load(null, null)
        val chain = arrayOf(cert, certificate)
        result.setKeyEntry(jks.alias, keyPair.private, jks.password, chain)
        return result
    }

    private fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        val secureRandom = SecureRandom.getInstance("SHA1PRNG")
        generator.initialize(1024, secureRandom)
        return generator.generateKeyPair()
    }

    private fun createSubjectKeyIdentifier(key: Key): SubjectKeyIdentifier? {
        val bIn = ByteArrayInputStream(key.encoded)
        var inputStream: ASN1InputStream? = null
        return try {
            inputStream = ASN1InputStream(bIn)
            val seq = inputStream.readObject() as ASN1Sequence
            val info = SubjectPublicKeyInfo.getInstance(seq)
            BcX509ExtensionUtils().createSubjectKeyIdentifier(info)
        } finally {
            closeSafely(inputStream)
        }
    }

    private fun signCertificate(
        certificateBuilder: X509v3CertificateBuilder,
        signedWithPrivateKey: PrivateKey
    ): X509Certificate {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signer =
                JcaContentSignerBuilder("SHA512WithRSAEncryption")
                    .build(signedWithPrivateKey)
            JcaX509CertificateConverter()
                .getCertificate(certificateBuilder.build(signer))
        } else {
            val signer: ContentSigner =
                JcaContentSignerBuilder("SHA512WithRSAEncryption")
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(signedWithPrivateKey)
            JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certificateBuilder.build(signer))
        }
    }

    private fun randomSerial(): Long {
        val rnd = Random()
        rnd.setSeed(System.currentTimeMillis())
        // prevent browser certificate caches, cause of doubled serial numbers
        // using 48bit random number
        var sl = rnd.nextInt().toLong() shl 32 or (rnd.nextInt().toLong() and 0xFFFFFFFFL)
        // let reserve of 16 bit for increasing, serials have to be positive
        sl = sl and 0x0000FFFFFFFFFFFFL
        return sl
    }

}