# This project uses git-flow 

You can download the git-flow plugin from: https://github.com/nvie/gitflow

The plugin only makes keeping track of branches easier and is not required. For the logic behind the branches see: http://nvie.com/posts/a-successful-git-branching-model/

## Initialize the local repository

This needs to be done for each local repository after cloning from github.

    git flow init -d

The -d is for accepting all defaults, which is what this project uses.

## Starting and publishing a feature branch

When you start working on a feature this is what you do:

    git flow feature start <name>

And when you want to push the branch to github so others can work on it:

    git flow feature publish <name>

## Tracking a feature branch

This is what you do when a feature branch has been created and published and you want to work on it locally and push changes back to github.

    git flow feature track origin <featurename>

Here featurename is the name after "feature/"

## Finishing a feature

When a feature is finished and needs to be merged back in to develop use this:

    git flow feature finish <name>