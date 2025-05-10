# Treasure Hunt
Author: spinloki

Version: 0.1.0

## Description
The goal of this mod is to make the hunt for rare blueprints and colony items more consistent and less rage-inducing.
It's still very early in development. You'll see some placeholder assets, and some aspects of it might be too overpowered.

It adds a new event intel which represents your ongoing efforts to find rare blueprints and colony items.
Like with the Hyperspace Topography event, it progresses naturally with normal gameplay, but can also be engaged with actively for faster progress.
Unlike regular salvaging, the rewards are far more consistent, and you get a fair degree of control over what you find.

## Treasure Hunt Intel Screen
A new intel entry appears in the campaign intel screen once the event begins. There are (currently) 3 stages at 100, 300, and 500 event progress.

### Found Lead
At 100 event progress, a random colony item or blueprint is selected from the list of items that you have not yet seen, or do not yet know.
An item is "seen" when it is selected as a lead, or if you loot it from somewhere while exploring naturally.

For example, if you loot a Pristine Nanoforge from a mothership before getting a lead on that item, you will not get a lead
on a Pristine Nanoforge again until you see all the other colony items. This way, you won't ever get a lead on a Combat Drone Replicator
10x in a row. If you loot that Pristine Nanoforge from the mothership after finding a lead on a Pristine Nanoforge, then
the lead will be unaffected. You will have two Pristine Nanoforges when you complete the hunt.

###### Abandon Lead
If you are not interested in the lead that you find at 100 event progress, you can use a new ability called Abandon Lead
(which you can assign to the ability hotbar) to reset the event progress to 0.
If the lead was a blueprint or blueprint package, that specific lead will never appear again.
If the lead was a colony item, it will not appear as a lead again until you have seen all colony items.

If you find a ship blueprint while exploring, while also having that same blueprint as a lead, you should immediately use this ability.
Otherwise, any further event progress will go to waste.

### Found Opportunity
This stage doesn't do anything yet, but it's next on the roadmap.

### Found Item
At 500 event progress, the hunt is complete. If the treasure is an item, it is placed directly in your cargo. 
If it is a blueprint, then it is added to your list of known blueprints.

## Progressing the Hunt
You can contribute to the treasure hunt through three main avenues:

### Explore
Surveying derelict stations and ships, and exploring planetary ruins contributes to the event. 
The new logistics hullmod `Treasure Hunt Package`, which is unlocked by default, boosts progress gained from one-time sources.

### Fight
Engage scavenger fleets—the same ones who sell hyperspace topography data. 
Destroying their ships grants event progress based on the value of the ships you destroy.
Alternatively, you can purchase salvage data from them directly, though the price is steep due,
as scavengers are reluctant to give up their trade secrets.

### Colonize
Colonies established on worlds with ruins passively generate treasure hunt progress each month, based on the size and richness of the ruins. 
Tech-mining further increases the contribution.
