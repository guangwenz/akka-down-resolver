Akka Split Brain Resolver
============

[![Build Status](https://travis-ci.org/guangwenz/akka-down-resolver.svg?branch=master)](https://travis-ci.org/guangwenz/akka-down-resolver)

What it is
----------
it provides split brain resolver for static akka cluster using quorum strategy.

## Setup ##

add dependency to your `build.sbt`

```scala
"org.guangwenz" %% "akka-down-resolver" % "1.2.4",
```

## Configuration ##

```scala
akka.cluster.downing-provider-class = "org.guangwenz.akka.cluster.SplitBrainResolver"
guangwenz.cluster.split-brain-resolver {
    active-strategy = static-quorum
    #the time to wait before resolving the split brain situation.
    stable-after = 7s

    static-quorum {
        # N / 2 + 1 is the recommend settings for this quorum size.
        quorum-size = 6
    }
}
```

check `reference.conf` for more config options.

Reference
---------
[Lightbend Split Brain Resolver Doc](http://developer.lightbend.com/docs/akka-commercial-addons/current/split-brain-resolver.html "Lightbend Split Brain Resolver Doc")
