<div align=center><img src="https://github.com/gaoxianglong/encryption-dog/blob/master/resources/logo.png"/></div>

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) ![License](https://img.shields.io/badge/build-passing-brightgreen.svg) ![License](https://img.shields.io/badge/version-2.0.2--RELEASE-blue)
> Encryption program with high performance, high security and rich functionsm.<br/>
> Supports binding the same physical device for file encryption and decryption.<br/>

## Use of EncryptionDog
### install
```shell
git clone git@github.com:gaoxianglong/encrypt-dog-new.git
mvn package
alias dog = 'java -Xms1g -Xmx1g -Xmn384m -jar dog-2.0.2.jar'
```
or
```shell
$ wget https://github.com/gaoxianglong/encrypt-dog-new/releases/download/v2.0.2/dog-2.0.2.jar
alias dog = 'java -Xms512m -Xmx512m -Xmn384m -jar dog-2.0.2.jar'
```
### use
```shell
$ dog -h
Welcome to
   ____                       __  _           ___
  / __/__  __________ _____  / /_(_)__  ___  / _ \___  ___ _
 / _// _ \/ __/ __/ // / _ \/ __/ / _ \/ _ \/ // / _ \/ _ `/
/___/_//_/\__/_/  \_, / .__/\__/_/\___/_//_/____/\___/\_, /
                 /___/_/                             /___/
	version: 2.0.3-RELEASE

Missing required options: '--secret-key', '--source-file=<source file>'
Usage: encrypt-dog [-dehoV] -k [-k]... [-a=<encryptAlgorithm>] [-t=<storage
                   path>] -s=<source file>[,<source file>...]... [-s=<source
                   file>[,<source file>...]...]...
  -a, --encrypt-algorithm=<encryptAlgorithm>
                     The default encryption algorithm is AES. Currently
                       supported encryption algorithms are AES, DESede (3DES),
                       and XOR.
  -d, --delete       The source file is not deleted after the default operation.
  -e, --encrypt      The default is decryption mode.
  -h, --help         Show this help message and exit.
  -k, --secret-key   Both encrypt and decrypt require the same secret key
  -o, --only-local   Encryption and decryption operations can only be performed
                       on the same physical device.Only Apple Mac is supported
  -s, --source-file=<source file>[,<source file>...]...
                     Target files that need to be encrypt and decrypt,Wildcards
                       are supported.
  -t, --target-path=<storage path>
                     The storage path after the operation is stored in the
                       original path by default.
  -V, --version      Print version information and exit.
Copyright(c) 2021 - 2031 gaoxianglong. All Rights Reserved.
```
### gui mode
EncryptionDog also provides a Swing graphical interface with a starry-sky particle theme. Add `--gui` to the startup command to launch it (the remaining arguments prefill the form):
```shell
$ java -Xms512m -Xmx512m -jar dog-2.0.3.jar --gui
# prefill source files and algorithm
$ java -jar dog-2.0.3.jar --gui -e -a AES -s /path/to/file1,/path/to/file2
```
Interface guide (all UI text is English, decorated with the app logo in the form header and title bar):
- **Mode**: switch between Encrypt / Decrypt. The confirm-key field only appears in encrypt mode.
- **Source files**: pick multiple files (`+ Select files`) or a directory (`+ Select directory`, recurses subdirectories); `Remove selected` removes entries.
- **Secret key**: enter the key (at least 6 digits); enter it twice when encrypting. The eye icon inside each key field toggles masked/plaintext display without losing input.
- **Algorithm**: AES (default) / 3DES / XOR.
- **Target directory**: leave blank to output next to the source files.
- **Options**: `Delete source files after operation` (asks again before executing), `Local machine only` (highest security, same as `-o`).
- After clicking Encrypt/Decrypt, a file-list confirmation dialog appears (equivalent to the terminal Y/N confirmations), then a progress panel shows per-file progress and estimated remaining time. On full success the particle background bursts; on failure the card shakes and the error is shown in red.
### highest security
Files encrypted on computer a can only be decrypted on computer a.<br/>
Principle:
<div align=center><img src="https://github.com/gaoxianglong/encryption-dog/blob/master/resources/hs.png"/></div>

WARN:
> Deleting or damaging the random key will never complete decryption.<br/>
```
### file structure
|  file        | file extension name |  type          |   magic number |   location   |    amount     |     ascii    |
|  :-:         |        :-:          |  :-:           |   :-:          |    :-:       |     :-:       |      :-:     |
| DOG FORMAT   |        .dog         |  u4/32bit      |   0xDE0225CF   |    header    |       1       |      ...     |
