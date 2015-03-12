## M3U COPY

A simple application to parse an [M3U](http://tools.ietf.org/html/draft-pantos-http-live-streaming-08) playlist, and copy it and referenced files to a new location.

The need for this comes from wanting to create playlists for my car's MP3-capable stereo system - I'd like to construct a list in an application like [Clementine](https://www.clementine-player.org/), and then easily transfer those files and the playlist itself to an SD card. 

I'm also interested in using RockBox with my iPod mini. It can also be managed like a simple external drive/SD card.

## FEATURES

### No duplicates
Avoid copying the same file if it already exists. 

### Path mangling
Paths in the provided M3U file need to be made relative to where the files are stored on the media.

## TECH
I'm using [clojure](http://clojure.org/), and [boot](http://boot-clj.com/) for this project.

Phase one will be implemented as a simple boot script.

A web interface may be useful in lieu of more robust (QT/GTK/Swing) UIs in the future.

If a database is used, consider bundling redis somehow, or use something like https://github.com/flausenhaus/clj-rocksdb.

Another option would be to store file info as an [EDN](https://github.com/edn-format/edn) file. This should be as efficient as using a key-valyue store, but it may slow startup time, or have less robust search features (since you interact with it using normal clojure mechanisms).

## NOTE
I'm sure something like this exists somewhere :) This project exists mostly for my own edification - this is an early hobby clojure project for me.

## FUTURE
I have ideas for the near-term once the basic functionality is complete:
  
  - use swing or another UI toolkit (QT has bindings for java) to construct a playlist manager application.
  - add playlist generation capabilities.  
  - support other playlist formats (why?).
  - construct/use a file metadata database.

## Future Features

### Robust UI
Provide a desktop and/or web UI to make using m3u-copy easier.

### Simple music DB
To make searching easier/faster, provide a simple searchable database.

The user can import one or more directories. 

### Auto-add To Music DB
Run a service to monitor the source directories and automatically add/remove/update files in the database to keep it fresh.

### Playlist Management
Inspect, create, and modify playlists.

### File Copy - Playlist
Create a playlist in a given format (M3U) and copy it to a given target. Copy all referenced files, but don't copy files already on the target.