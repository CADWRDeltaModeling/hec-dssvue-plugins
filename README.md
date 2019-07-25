# HEC DSSVue Plugins 
**[Download here](https://data.cnra.ca.gov/dataset/hec-dssvue-plugins)**

# Usage
Follow the above download link to get to the site. The usage and install instructions are on that site in more detail

## Introduction
Plugins written to extend HEC-DSSVue

## Godin Filter
Godin filters are used to remove tidal signals from timeseries of stage, flow and other such variables. This plugin adds a menuitem "Godin Filter" to the "Tools" menu. Select the pathname and then click on this menu item and it will add the godin filtered pathname to the same dss file. The current time window is honored so please be sure to clear it if you want to calculate it for the entire window of that pathname.

## CDEC download filter
CDEC is California DWR's repository of water data available as a web service. This plugin adds a menu item to the "Data Entry" | "Import" item. It is similar to the one offered by the HEC-DSSVue but has easier lookup as well as faster downloads (via parallel downloads)
