package spider

import org.eclipse.jgit.transport.JschConfigSessionFactory
import com.jcraft.jsch.JSch
import org.eclipse.jgit.util.FS
import org.eclipse.jgit.transport.OpenSshConfig
import com.jcraft.jsch.Session

/**
 * Created by a.maximov on 24.05.2014.
 */
public class CustomConfigSessionFactory : JschConfigSessionFactory() {
        val pathToDotSSH = "C:/cygwin64/home/a.maximov/.ssh"


    override fun configure(hc: OpenSshConfig.Host?, session: Session?) {

    }

    override fun getJSch(hc: OpenSshConfig.Host?, fs: FS?): JSch? {
        val jsch = super.getJSch(hc, fs)
        jsch?.removeAllIdentity()
        jsch?.addIdentity( "$pathToDotSSH/123qwe" )
        return jsch
    }
 }
