<div align=center><img src="https://raw.githubusercontent.com/gaoxianglong/encrypt-dog-new/refs/heads/master/resources/logo-encryptdog.png"/></div>

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) ![License](https://img.shields.io/badge/build-passing-brightgreen.svg) ![License](https://img.shields.io/badge/version-2.0.5--RELEASE-blue)
> Encryption program with high performance, high security and rich functionsm.<br/>
> Supports binding the same physical device for file encryption and decryption.<br/>

## Use of EncryptDog
### install
#### macOS App (recommended)
Download the DMG, drag EncryptDog into Applications, and launch the app — the graphical interface opens directly:
```shell
$ wget https://github.com/gaoxianglong/encrypt-dog-new/releases/download/v2.0.5/EncryptDog-2.0.5.dmg
```
#### jar (also supported)
```shell
$ wget https://github.com/gaoxianglong/encrypt-dog-new/releases/download/v2.0.5/encryptdog-2.0.5.jar
alias dog = 'java -Xms1g -Xmx1g -Xmn384m -jar encryptdog-2.0.5.jar'
```
#### build from source (also supported)
```shell
git clone git@github.com:gaoxianglong/encrypt-dog-new.git
mvn package
alias dog = 'java -Xms1g -Xmx1g -Xmn384m -jar encryptdog-2.0.5.jar'
```
### gui mode
EncryptDog's primary interface is a Swing graphical UI with a purple gradient theme. DMG users launch the app directly; jar users add `--gui` to the startup command (the remaining arguments prefill the form):
```shell
$ java -Xms1g -Xmx1g -Xmn384m -jar encryptdog-2.0.5.jar --gui
# prefill source files and algorithm
$ java -jar encryptdog-2.0.5.jar --gui -e -a AES -s /path/to/file1,/path/to/file2
```
### terminal mode
A terminal mode is also available (run `dog` without `--gui`; its behavior is unchanged by the GUI):
```shell
$ dog -h
Welcome to
   ____                       __  ___
  / __/__  __________ _____  / /_/ _ \___  ___ _
 / _// _ \/ __/ __/ // / _ \/ __/ // / _ \/ _ `/
/___/_//_/\__/_/  \_, / .__/\__/____/\___/\_, / 
                 /___/_/                 /___/
	version: 2.0.5

Usage: encryptdog [-dehoV] -k [-k]... [-a=<encryptAlgorithm>] [-t=<storage
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
`-k` is interactive: the key is typed at a masked prompt (it is not passed on the command line). Encrypt asks for it twice, decrypt once; the file list is then confirmed with Y/N:
```shell
# encrypt file.txt -> file.txt.dog (key entered twice at the masked prompts)
$ dog -e -k -s /path/to/file.txt
# decrypt file.txt.dog -> file.txt (key entered once)
$ dog -k -s /path/to/file.txt.dog
```
### highest security
Files encrypted on computer a can only be decrypted on computer a.<br/>
Principle:
<div align=center><img src="https://github.com/gaoxianglong/encryption-dog/blob/master/resources/hs.png"/></div>

WARN:
> Deleting or damaging the random key will never complete decryption.<br/>
### file structure
|  file        | file extension name |  type          |   magic number |   location   |    amount     |     ascii    |
|  :-:         |        :-:          |  :-:           |   :-:          |    :-:       |     :-:       |      :-:     |
| DOG FORMAT   |        .dog         |  u4/32bit      |   0xDE0225CF   |    header    |       1       |      ...     |
