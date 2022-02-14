An app that parses the Path of Exile Archnemesis inventory and help and shows an overlay to help
with picking which ones to use/craft.

## Quick start
1) Go to the [releases page](https://github.com/Amuxix/PoEArchnemesis/releases) and downloaded
the zip from the latest version.
2) Extract the zip.
3) Open the bin folder
4) Run `run.bat`

## Keys
By default the keys used are:
- `Esc` - Close the overlay
- `F1` - Show help
- `F2` - Reopen the window with the last parse
- `F3` - Open the window and parse (requires arch nemesis inventory to be visible)
- `Left click` - Cycle betwen marking an Archnemesis to be crafted/used when possible
- `Right click` - Toggle marking the extraction of the Archnemesis mapping

## Example usage steps
1) Open the Archnemesis inventory
2) Press F3 and wait for the parsing to be completed
3) Select which Archnemesis you want to craft/use
4) Move mouse outside the overlay
5) The Archnemesis should now be filtered to reflect the Archnemesis you selected

## Mappings
Mappings are simple a list of color codes corresponding to each Archnemesis.
They are required to parse the inventory and vary according to screen resolution, for 1440p
some are already included but need to be moved to the right place.

The included mappings are in the root of the zip file and need to be moved inside the conf folder.

### Extracting Mappings
1) Put all Archnemesis you want to extract in the top of the inventory starting from the top left going right until
line is ful then start on the next line
2) Mark the Archnemesis in the order they are in the inventory.
3) Move mouse off the overlay for it close

### Configuration
To parse the inventory this reads part of the screen, the coordinates of which are found in the configuration files.

Two configs are provided for 1080p and 1440p, **1440p is the default one**, to change to 1080, open `application.conf` and on the last line replace 1440 with 1080
General configs such as keys are found in `application.conf`
