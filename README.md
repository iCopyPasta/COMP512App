
<h1>Java Files' Class description</h1>
<ul>
<li>BonusRoundFragment - handles local bonus round logic</li>
<li>CodenameGenerator - generates friendly names in a random fashion</li>
<li>GameStateContainer - contains the global state view that a peer believes to be true</li>
<li>JoinPeerAlert - dialog to let a user know whether or not to join someone</li>
<li>MainActivity - the main screen with "host" or "find opponent" screen</li>
<li>PeerDataItem - class to encapsulate an item on the screen of joinable peers</li>
<li>PeerListItemsFragment - handles the view for the list of joinable peers</li>
<li>PeerState - class to encapsulate a single node in the network</li>
<li>TextFight - the controlling activity for all three visiual fragments</li>
<li>TextMainArenaFragment - handles the local regular game logic</li>
</ul>

<h2>Issues and general location in code</h2>

<h3>Global State View</h3>
<ul>
<li>Main logic for processing is in TextFight in onPayloadReceived</li>
<li>PeerState and GameStateContainer objects are used for broadcasting back and forth.</li>
</ul>

<h3>Voting/Declaration of a Winner:</h3>
<ul>
<li>onSetWinnerSnapshot and gatherVotes in TextFight</li>
<li>claim victory methods in BonusRoundFragment and TextMainArenaFragment</li>
</ul>

<h3>Peer Connection/Topology:</h3>
<ul>
<li>Handled in TextFight in various methods, including connectionLifecycleCallback object with method onDisconnnected, and general rebroadcasting in TextFight in onPayloadReceived</li>

</ul>

<h3>Message Passing</h3>
<ul>
<li>TextFight in onPayloadReceived</li>
<li>onSendBroadcast which calls the API's sendPayload method</li>
</ul>
 

