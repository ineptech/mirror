# Mirror
Fully configurable "Magic mirror" Android app intended to run on a tablet fitted behind a two-way mirror.



About
--

This is a simple app to display useful information on a tablet that is mounted behind a two-way mirror 
so that the text appears to float on the mirror.  I've had it running in my house for a few months and I love it - 
it is both a great conversation piece and genuinely useful.  

This isn't the first, or even the tenth or twentieth, mirror app on github, so feel free to search for others if this 
one doesn't suit your fancy.  The major difference is that this one has enough setup/config options to make it 
usable for non-programmers.  Most users should be able to just get the app from the Google Play store 
and configure it to suit their needs without altering the code.  Of course, if you're comfortable doing Android 
development, you can just fork it and modify it to suit your needs and install directly to the tablet.

Features
--
* Time, Day of week
* Birthdays and Holidays (configurable)
* Appointments and reminders from Google Calendar
* Current temperature and high/low for the day (from forecast.io)
* Stock quotes (from yahoo)
* Time til your bus/train arrives (requires some technical savvy to set up, but no coding required)
* Configurable brightness and font sizes

Build the mirror
--
1. Acquire an Android tablet.  I'm using a Nexus 7 I got off Craigslist for $50.
2. Acquire a frame.  I bought one at a frame shop for $30.  Recommend one with enough depth that the tablet won't hit the wall.
3. Acquire a two-way mirror that fits the frame.  Some other guides recommend acrylic mirrors - I **strongly** discourage this!  
I tried that, and the 3/8" acrylic was way too bendy, it looked like a fun-house mirror.  I'm using an 11x14 glass mirror from 
http://www.twowaymirrors.com/.  It cost $48 (about the same as acrylic) and looks perfect.
4. Make a back with a tablet-sized hole in it.  I did this with an old piece of cardboard and some velcro.  I recommmend you 
add a piece of construction paper in between the cardboard and the tablet with a slightly smaller hole, to make sure 
no light gets in around the edges.  
5. Acquire a USB charging cable long enough to reach an outlet.  About $5 from Amazon.  I happened to have a hole in 
my living room wall from an old security system the previous owner had installed, so I was luckily able to wire the power 
cord inside the wall, which makes the mirror look much cleaner.

Configure the app
--
1. The configuration screen launches automatically when you run the app.  (If you need to get back to it, "hard close" the 
app (by hitting the sqaure "current apps" menu button and swiping the app away) and re-launch it).  
2. Each module has a checkbox to disable - un-check any modules you don't want to use.
3. Each module has a widget for adjusting the text size.  Note that there is some sample output to gauge how the text 
size will look once the app is launched.  Adjust it however you like.
4. In the "Birthdays" module, set the Date and Name of each person you'd ilke to see a "Happy Birthday" message for and 
hit the plus sign to add them.  Same for "Holidays".
5. For the weather module:
 * Go to developer.forecast.io and hit "Register".  Follow the instructions to receive a (free) api key.  
 * Copy it (double-tap to highlight, press-and-hold to copy) and paste it (press-and-hold to paste) your api key in to the 
 api key field.
 * Look up your latitude and longitude and set them in the designated fields.
6. Add any stocks you'd like to track by typing in the ticker code and hitting the little plus sign.
7. Configure your appointments, reminders, etc however you'd like them to appear *in the Google Calendar app* (not 
the mirror).  Keep in mind that Google Calendar lets you configure things on a per-device basis - for example, you can 
have your "work" appointments show up on your phone but not your mirror by simply opening the Gooogle Calendar app 
on the mirror tablet and unchecking the "work" calendar.  Also note that you can add a user other than the tablet's owner
and run the mirror as that user, and that user's calendar appointments (not the tablet owner's) will show up.
8. Mass Transit is a little tricky.  You will need to consult the documentation for your local transit service to 
figure out what URL to query, and specify a regular expression to extract the value you want (time until next bus or train) 
from the response.  You may also have to sign up for a key similar to the one you got for forecast.io.  This is what I 
had to do to get it working in Portland, OR:
 * Look up the local transit service's technical docs at http://developer.trimet.org/ and request a key
 * Following their documentation, construct a URL to query the train stop by my house
 * Paste that URL in to a web browser and look at the response
 * Copy that response in to the "text to test" field of myregexp.com
 * Construct a regex that "captures" the time of the next bus/train and nothing else.  (Regular expressions are complicated! 
 If you're not familiar with them, find a friend who is and offer beer in exchange for help.  Losing a couple beers 
 in exchange for not having to learn how regular expressions work is a *bargain*.)
 * Enter the URL and Regex in the appropriate fields and use the "Test" button to see if it worked.
 * Enter a name and hit the Plus sign to add this Transit item.  Note that you can add more than one.  Also note that, in 
 the name field, "Train" will be replaced by a picture of a train, and "Bus" with a picture of a bus.
 9. Configure the Brightness controls.  This is tedious but worth it - getting the brightness just right for your environment
 makes the mirror look a lot better.  Too dim, and you won't be able to comfortably see the time/date from across the 
 room; too bright and you will see a rectangular "halo" at the edges of the tablet.  Use values between 0 and 1.  
 I recommend setting the default brightness around 0.5, and then adding a setting of .8 for around 9AM-2PM (or whenever 
 your room is brightest) and a setting of .1 for the bedtime-sunrise range.  
 9. Hit Save.  The Mirror display will launch, and should stay visible until the app is closed manually.  The modules 
 that show remote data should update themselves every time the minute changes.  
 
About
--
This app was written by Nick Hall.  It and its code are free for anyone to use for any purpose.  It was inspired by 
[HannahMitt's mirror](https://raw.githubusercontent.com/HannahMitt/HomeMirror).  My mirror has been running in my house for 
about three months now and I am really happy with it.  It is both a great conversation piece and a genuinely useful 
piece of furniture.  I initially made it for my own use, and then added all the config stuff so that a couple of my 
non-programmer friends could make one as well.   
 
FAQ
--
* Can you swipe the tablet through the glass?  Have you thought about voice commands?  What about gesture/face recognition 
through the camera?
There are other mirror projects that do things like this, but I consciously chose not to implement any of this because 
my house is already littered with tablets and laptops to interact with.  I treat my mirror like a clock - 
glance at it when you want information, ignore it otherwise. 

* Why do I have to configure brightness manually when most Android tablets include a brightness sensor?  
On my tablet, the sensor is not very accurate at the best of times.  Once the tablet is mounted behind the mirror, the 
sensor is completely worthless.  YMMV.  Another option would be to try to set it based on the weather (how sunny the 
forecast.io service thinks it is) but I have not pursued this.

