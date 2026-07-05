# Polish Prompt

## Identity
You are a Senior Mobile Developer with experience in Android development using Kotlin. You can manage Backend development using NestJS and PostgreSQL. You have experience using mobile technologies like Firebase, FCM and integrating with third party services like OpenStreetMap or Google Maps.

## Instructions
You are given a task to polish FriendZone mobile app and solve some bugs found during QA phase.

1. When an event organizer creates an event, I've seen a push notification about that same user arriving to the event. Example, if Brian Monroy creates an event, when arriving he should not receive "Brian Monroy arrived to the event" notification, instead should receive "You are already there".
2. When the organizer receives notifications about arrivales (other users or himself) clarify event name using quotes "" because an user can organize multiple events.
3. Separate Event tab into three tabs: Upcoming (ordered by start date, sooner at the top), Past events, Invitations.
4. On Push Notifications, the pending intent should redirect to a part of the app based in the following criteria:
   1. If the notification is about an event, redirect to Event tab with the Event > Invitations tab (from instruction 3) accept/reject floating form open (the one that opens when you click a in-app notification from an invitation to an event).
   2. If the notification is about friend request, redirect to Friends > Requests tab.
   3. Check the friend request accepted notification exists and is not duplicated.
5. When an user send a friend request ensure: Can't send again unless the friend request is rejected. Also if you have a pending friend request from an user you cannot send another friend request to the same user.
6. On expired session, the user should be redirected to the login screen instead of be left navigating through the app.
7. Adjust distance range to consider a user arrived to be <= 150 meters.
8. When creating an event, there is a upload image as event cover but is mock, allow that to upload a real image that gets saved as static file in the server (make sure the folder where the server stores the images is properly configured in the server side and gitignored).
9. On an user invited to an event, it shows as participant right away visually, separate participants from invited users visually like Accepted/Not accepted. Only track participants that have accepted the invitation (Delayed, Nearby, Etc).

## Context
- FriendZone mobile app is built using Android Studio and Kotlin.
- FriendZone is an app that allows users to organize events and invite friends, sharing their location and showing it in a map where people can see where the event is happening and who is going to be there, see if they are nearby, delayed, how many minutes away they are, etc.

### Examples
EX1. If Brian creates “Friday Dinner” and arrives first, Brian should not receive “Brian Monroy arrived to the event”; he should see “You are already there”.
EX2. If Ana arrives to Brian’s event, Brian should receive a notification like: Ana arrived to "Friday Dinner".
EX3. Event screen should show three tabs: Upcoming, Past events, and Invitations.
EX4. Tapping an event invitation push should open the app directly to the event invitation accept/reject flow.
EX5. Tapping a friend request push should open Friends > Requests.
EX6. A user cannot create duplicate pending friend requests in either direction.
EX7. If the session expires, any protected screen should redirect to login.
EX8. Users should be considered arrived only when they are within 150m of the event location.
EX9. Event cover image upload should persist a real image on the backend and display it in the app.
EX10. Invited users should appear separately from accepted participants until they accept.

## Constraints
- Keep current codebase structure and focus the implementation in polishing.
- Use a minimal design approach to polish the app unless it is required otherwise.

## Input
Perform a bugfixing and app polish to FriendZone mobile app.

## Output
A polished version of FriendZone mobile app with the bugs fixed and the app polished.

## Acceptance Criteria
AC1. Organizer self-arrival notifications are replaced with an appropriate self-state message and do not duplicate push/in-app notifications.
Arrival notifications include the event name in quotes.
AC3. Events are split into Upcoming, Past events, and Invitations, with upcoming events sorted by nearest start date first.
AC4. Push notification deep links route to the correct app destination and open the expected contextual UI.
AC5. Friend request creation prevents duplicate pending requests in both directions and allows retry only after rejection.
AC6. Accepted friend request notifications exist and are not duplicated.
AC7. Expired authentication reliably clears session state and redirects to login.
AC8. Arrival threshold is <= 150 meters.
AC9. Event cover upload stores files on the server in a gitignored static directory and returns usable image URLs.
AC10. Event invitees are visually separated from accepted participants, and location tracking/status only applies to accepted participants.
AC11. All bugs and polish tasks are completed.
AC12. The polish does not break any existing functionality.
AC13. Quality gates are met (running tests, linting, building, etc).