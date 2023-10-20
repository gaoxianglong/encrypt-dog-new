<div align=center><img src="https://github.com/gaoxianglong/encryption-dog/blob/master/resources/logo.png"/></div>

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) ![License](https://img.shields.io/badge/build-passing-brightgreen.svg) ![License](https://img.shields.io/badge/version-1.6.0--RELEASE-blue)
> Encryption program with high performance, high security and rich functionsm.<br/>
> Supports binding the same physical device for file encryption and decryption.<br/>

## Use of EncryptionDog
### install
```shell
git clone git@github.com:gaoxianglong/encrypt-dog-new.git
mvn package
alias dog = 'java -Xms1g -Xmx1g -Xmn128m -jar dog-2.0.0.jar'
```
or
```shell
$ wget https://github.com/gaoxianglong/encrypt-dog-new/releases/download/1.6.0-RELEASE/dog-1.6.0.jar
alias dog = 'java -Xms512m -Xmx512m -Xmn384m -jar dog-2.0.0.jar'
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
	version: 2.0.0-RELEASE

Missing required options: '--secret-key', '--source-file=<source file>'
Usage: encrypt-dog [-dehoV] -k [-k]... [-a=<encryptAlgorithm>] [-t=<storage
                   path>] -s=<source file>[,<source file>...]... [-s=<source
                   file>[,<source file>...]...]...
  -a, --encrypt-algorithm=<encryptAlgorithm>
                     Set the algorithm type to use 3DES encryption algorithm by
                       default.
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
| DOG FORMAT   |        .dog         |  u4/32bit      |   0x19890225   |    header    |       1       |      ...     |
