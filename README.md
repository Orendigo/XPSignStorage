# XPSignStorage
Store Minecraft XP within signs

# About
This is a recreation of an old Minecraft plugin I used in the past, but have since lost the ability to find.  This was originally going to be a fork of [this repository](https://github.com/GD-gh/XPBank-Spigot) which I felt was very similar to the original plugin I used, but as I progressed I strayed further from this repository to a point where I did not believe it could be considered a fork other than mentioning it here.

# Usage
Write '[XP]' on the first line of a sign to create an XP sign.  The sign will automatically generate the rest of the information.  Right-click to add XP to a sign, crouch + right-click to remove XP from a sign.  Currently the only option is to transfer all XP at once, which may be editable in the future.

# Interesting Notes
This plugin uses an [SQLite Database implementation](https://www.spigotmc.org/threads/how-to-sqlite.56847/) to save the owner, amount, and location of the XP sign, rather than relying on the physical sign to store the information.  As such, the signs are impervious to being edited via external sign-editing plugins, and will retain their stored XP upon recreation in the same location if they are broken via unintentional means (piston movement, leaf decay, block physics, etc).
