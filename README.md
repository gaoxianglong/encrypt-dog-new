<div align=center><img src="https://github.com/gaoxianglong/encryption-dog/blob/master/resources/logo.png"/></div>

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) ![License](https://img.shields.io/badge/build-passing-brightgreen.svg) ![License](https://img.shields.io/badge/version-2.0.4--RELEASE-blue)
> Encryption program with high performance, high security and rich functionsm.<br/>
> Supports binding the same physical device for file encryption and decryption.<br/>

## Use of EncryptionDog
### install
```shell
git clone git@github.com:gaoxianglong/encrypt-dog-new.git
mvn package
alias dog = 'java -Xms1g -Xmx1g -Xmn384m -jar dog-2.0.4.jar'
```
or
```shell
$ wget https://github.com/gaoxianglong/encrypt-dog-new/releases/download/v2.0.4/dog-2.0.4.jar
alias dog = 'java -Xms1g -Xmx1g -Xmn384m -jar dog-2.0.4.jar'
```
### gui mode
EncryptionDog's primary interface is a Swing graphical UI with a starry-sky particle theme. Add `--gui` to the startup command to launch it (the remaining arguments prefill the form):
```shell
$ java -Xms1g -Xmx1g -Xmn384m -jar dog-2.0.4.jar --gui
# prefill source files and algorithm
$ java -jar dog-2.0.4.jar --gui -e -a AES -s /path/to/file1,/path/to/file2
```
Interface guide (all UI text is English, the app logo and brand name appear in the title bar, only macOS is supported):
- **Mode**: Encrypt / Decrypt segmented switch in the first row of the card. In Decrypt mode both the confirm-key row and the `Local machine only` option are hidden (decryption never uses them).
- **Source files**: drag & drop files or folders into the drop zone (purple breathing border while hovering, ripple and row fade-in on drop, duplicates ignored); or use `+ Select files` / `+ Select directory` (recurses subdirectories) and remove entries with `Remove selected`.
- **Secret key**: enter the key (at least 6 digits); enter it twice when encrypting (decrypt asks once). The eye icon inside each key field toggles masked/plaintext display without losing input.
- **Algorithm**: AES (default) / 3DES / XOR. Selecting XOR in encrypt mode shows a "Use with caution" warning next to the dropdown.
- **Target directory**: leave blank to output next to the source files; a Browse… button is embedded in the field.
- **Options**: `Delete source files after operation` (a themed confirmation dialog asks again before executing), `Local machine only` (highest security, same as `-o`; encrypt mode only).
- **Validation**: failed checks show as inline hints inside the offending field (key errors in the key fields, file errors in the drop zone) or above the main button when there is no specific field; hints fade out after about 2 seconds. Files to encrypt must not end with `.dog`; files to decrypt must end with `.dog`.
- **Execution**: submitting starts the operation directly — only when `Delete source files after operation` is checked does a themed confirmation dialog ask again first. The window then switches directly to a wide layout (~1200×800). A stats row (Operation / Files / Success / Failed / Elapsed) and a per-file table (No, Source File, Before Size, After Size, State, Progress, Estimated Time, Target File, Result) update in real time with per-file progress bars and percentages; hovering a truncated path shows the full value and hovering a failure icon shows the reason. On completion the app stays on this page and plays a chime; execution errors are shown in red at the top of the card.
- **Back**: the arrow button under the title bar returns to the form, resetting everything to its initial state (Encrypt mode, AES, all fields and files cleared). Failures never auto-return; the result page stays until you go back yourself.
### terminal mode
A terminal mode is also available (run `dog` without `--gui`; its behavior is unchanged by the GUI):
```shell
$ dog -h
Welcome to
   ____                       __  _           ___
  / __/__  __________ _____  / /_(_)__  ___  / _ \___  ___ _
 / _// _ \/ __/ __/ // / _ \/ __/ / _ \/ _ \/ // / _ \/ _ `/
/___/_//_/\__/_/  \_, / .__/\__/_/\___/_//_/____/\___/\_, /
                 /___/_/                             /___/
	version: 2.0.4

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
