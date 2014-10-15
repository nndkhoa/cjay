cjay
====

Android client for CJay Network

### Status
[![Build Status](https://magnum.travis-ci.com/tieubao/cjay.svg?token=MqnrksRWYEqTBB3yeGpj&branch=master)](https://magnum.travis-ci.com/tieubao/cjay)

## Libs

- Android Annotation:

- Android Priority JobQueue

- EventBus

- cwac camera

- Retrofit

- SnappyDB

- Android Crashlytics


## Coding Convention

Please read and follow convention in [Wiki](https://github.com/tieubao/cjay/wiki/Android-Coding-Convention)

## Background tasks

There are 2 kinds of background task:

- Need to execute instantly after the time you call them. e.g. fetch all sessions, operators, iso codes, search sessions ... For this kind of tasks, we use `@Background` in `DataCenter` then `post an Event` after they had completed.

- Do not need to run at this time but as soon as possible. e.g. upload images, upload container sessions ... For this kind of tasks, we should use `JobQueue` to let them execute jobs in queued style.

## README Driven Development

To make sure that every people can understand your code, your algorithm and workflow, we will apply a simple version of documentation-first software process (DDD) which called [`README Driven Development`](http://tom.preston-werner.com/2010/08/23/readme-driven-development.html).

You need to write out what you are going to do with this task on README file or [below the issue](https://github.com/tieubao/cjay/issues/193#issuecomment-57143529)

Another thing is that you should spend time writing comments in code carefully.