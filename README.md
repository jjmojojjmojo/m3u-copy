## M3U COPY

A simple application to parse an [M3U](http://tools.ietf.org/html/draft-pantos-http-live-streaming-08) playlist, and copy it and referenced files to a new location.

The need for this comes from wanting to create playlists for my car's MP3-capable stereo system - I'd like to construct a list in an application like [Clementine](https://www.clementine-player.org/), and then easily transfer those files and the playlist itself to an SD card. 

I'm also interested in using [RockBox](http://www.rockbox.org/) with my iPod mini. It can also be managed like a simple external drive/SD card.

## INSTALLATION
This application was developed on OSX. It should run well on any other unix-style operating system (e.g. Linux). Windows support is probable but untested. Binary releases will happen... someday :).

Check out this repository to a useful location, like `~/m3ucopy`:

```
$ cd ~
$ git clone https://github.com/jjmojojjmojo/m3u-copy.git
```

Install/Upgrade [boot](http://boot-clj.com/):

```
$ wget https://github.com/boot-clj/boot/releases/download/2.0.0-rc12/boot.sh
$ mv boot.sh boot && chmod a+x boot && sudo mv boot /usr/local/bin
```

If you've installed boot before, you may need to update it:

```
$ boot -u
```

Now you can run `./m3ucopy` and start moving files.

### Advanced Installation W/O Jars
**Note:** This is theorhetical at the moment!

The `m3ucopy` script can be copied anywhere, it just needs to be modified so boot knows where to find the source code:

```
$ sudo mkdir /opt/m3ucopy
$ sudo chmod -r 775 /opt/m3ucopy
$ git clone --depth 1 https://github.com/jjmojojjmojo/m3u-copy.git /opt/m3ucopy
$ sudo mv /opt/m3ucopy/m3ucopy /usr/local/bin
$ sudo chmod +x /usr/local/bin/m3ucopy
$ sudo vi /usr/local/bin/m3ucopy
...
```

This is the line to change:

```clojure
(set-env!
  :source-paths #{"src"}
  :dependencies '[[me.raynes/fs "1.4.6"]
                  [pandect "0.5.1"]
                  [green-tags "0.3.0-alpha"]])
```

Change the `:source-paths` setting to `#{"/opt/m3ucopy"}`. For more information on what `set-env!` does, check out [the boot documentation](https://github.com/boot-clj/boot/wiki/Boot-Environment).

## USAGE
The current incarnation of m3ucopy runs as a [boot](http://boot-clj.com/) script that uses the local codebase to do its thing. 

```
$ ./m3ucopy -h
Usage: m3ucopy [options] [source] [dest]

Options:
  -h, --help             Print this help info.
  -d, --dry-run          Dry-run mode. Just print what would be done to stdout
  -a, --alg-style STYLE  Set specify the path style. Options are: same, uid, artist-album artist-album-path. Default is 'same' to STYLE.
  -s, --spaces-ok        Are spaces OK? If not, they will be replaced with underscores. Defaults to true.
```

Dry-run mode writes the output m3u file to a temporary path, and displays what would happen to the console.

Spaces-ok controls if spaces are replaced in filenames. Some other special characters ([?/\,]) are replaced in any case.

The path style setting controls how paths will be created relative to the m3u file:

**same**

    Whatever the file was called (without regard to directory structure) will be used to name the 
    file when copied.
    
**uid**

    A simple md5 hash based on the tag information will be used to name the file. (e.g. 52a856749f12bfb2b6e9678cf29eeb15.mp3)
    
**artist-album**

    The file will be named Artist - Album - Track# - Title.ext (e.g. Lucas - Lucacentric - 002 - Lucas With The Lid Off.mp3)
    
**artist-album-path**

    The file will be put into an artist and album sub-directory. (e.g. Lucas/Lucacentric/002 - Lucas With The Lid Off.mp3)

## FEATURES

### No duplicates
Avoid copying the same file if it already exists. Makes it easy to manage files using multiple tools.

### Path mangling
Paths in the provided M3U file need to be made relative to where the files are stored on the media.

Paths can be generated as well using tagging information from the source files.

### Dry-run Mode
See what will happen without manipulating the data.

## TECH
I'm using [clojure](http://clojure.org/), and [boot](http://boot-clj.com/) for this project.

Phase one will be implemented as a simple boot script.

A web interface may be useful in lieu of more robust (QT/GTK/Swing) UIs in the future.

If a database is used, consider bundling redis somehow, or use something like [rocksdb](https://github.com/flausenhaus/clj-rocksdb).

Another option would be to store file info as an [EDN](https://github.com/edn-format/edn) file. This should be as efficient as using a key-valyue store, but it may slow startup time, or have less robust search features (since you interact with it using normal clojure mechanisms).

## NOTE
I'm sure something like this exists somewhere :) This project exists mostly for my own edification - this is an early hobby clojure project for me.

## NEAR TERM GOALS
The main thing is adding tests. Lots and lots of tests.

### Refinements and Enhancements
* Let the user specify the name of the target m3u file (currently it uses the name of the source playlist).
* Verify the copy-check is sane.
* Estimate size of files to copy and warn user if it exceeds available space.
* Figure out exactly which characters are OK or not for most file systems; allow the user to specify what's OK and what to replace it with.
* Let the user construct their own paths using a template string.
* Derive missing id3 tag information from path names.
* Let the user shuffle playlist order when writing.
* When album/artist sub-directories are not used, there may be issues with certain players or filesystems where a long directory listing may degrade performance. Verify this is really a problem, and allow for sub-directories to be generated to group files into manageable chunks. 
* Investigate the utility of copying files in parallel to make things faster.
* Remove embedded album art tags to save space for players that don't support it.

## FUTURE
I have ideas for the near-term once the basic functionality is complete:
  
  - use swing or another UI toolkit ([QT has bindings for java](http://briancarper.net/blog/398/)) to construct a playlist manager application.
  - add playlist generation capabilities.  
  - support other playlist formats (why?).
  - construct/use a file metadata database.

### Future Features

#### Robust UI
Provide a desktop and/or web UI to make using m3u-copy easier.

#### Simple music DB
To make searching easier/faster, provide a simple searchable database.

The user can import one or more directories. 

#### Auto-add To Music DB
Run a service to monitor the source directories and automatically add/remove/update files in the database to keep it fresh.

#### Playlist Management
Inspect, create, and modify playlists.