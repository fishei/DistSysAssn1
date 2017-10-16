Distributed Twitter

FILE STRUCTURES:

tweets.txt:	<userId>,<logical timestamp>,<tweet text>,<UTC timestamp>

clocks.txt:	<userId>; <userId>,<time>; <userId>,<time>; ...

blockList.txt:	<userId>, <userId>, <userId>,...

partialLog.txt:	<userID>,<logical timestamp>,<event data>
		where <event data> 	 = <type=tweet>,<text>,<utc timestamp>
			  		 = <type=block>,<userId>
			  		 = <unblock>,<userId>

users.txt: 	<userId>,<userName>,<ip address>

usage: java -jar DistributedTwitter.jar <userId> <dataDirectory>
