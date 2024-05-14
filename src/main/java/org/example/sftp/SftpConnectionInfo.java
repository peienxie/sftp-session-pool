package org.example.sftp;

public record SftpConnectionInfo(String host, int port, String username, String password) {
}
