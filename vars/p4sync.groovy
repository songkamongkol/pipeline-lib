import groovy.transform.Field

// we want this to be global so that we can consistently
// sync the same changelist over the course of our pipeline
@Field currentChangelist = ''


def p4sync(Map config = [:]) {

  echo "Config: ${config}"
  // needs config.view and config.credentialID
  if (!config.view && !config.credential) {
      error("You must provide either a view spec and p4 credential.")
  }
  
  def jenkinsWorkspaceName = "${JOB_NAME}-${STAGE_NAME}-${NODE_NAME}"
  ws(jenkinsWorkspaceName) {
    echo "[INFO] [p4] Running in ${pwd()}"
    echo "[INFO] [p4] Using P4 Workspace ${jenkinsWorkspaceName}"

    def defaultConfig = [
        workspacePattern: jenkinsWorkspaceName,
        syncType: 'AutoCleanImpl',
        stream: '',
        view: '',
        quiet: true,
        have: true,
        charset: 'utf8',
        parallel: false,
        clobber: true,
    ]

    defaultConfig.view = viewSpecWithWorkspace(config.view, config.workspacePattern)

    // do the actual sync
    syncConfig(defaultConfig)

    pwd()
  }
}

def call(Map p4Config = [:]) {
  // do the actual p4 sync using the p4 plugin
  p4sync(
    credential: p4Config.credentialsId,
    populate: [$class: p4Config.syncType,
      have: p4Config.have,
      modtime: false,
      parallel: [
        enable: p4Config.parallel,
        minbytes: '1024',
        minfiles: '1',
        path: '/usr/local/bin/p4',
        threads: '4'
      ],
      // specify the changelist to sync (syncs latest if nothing provided)
      pin: currentP4Changelist(p4Config.changelist),
      quiet: p4Config.quiet,
      revert: true,
    ],
    workspace: [$class: 'ManualWorkspaceImpl',
      charset: p4Config.charset,
      name: p4Config.workspacePattern,
      pinHost: false,
      spec: [allwrite: false,
        clobber: p4Config.clobber,
        compress: false,
        line: 'LOCAL',
        locked: false,
        modtime: false,
        rmdir: false,
        streamName: p4Config.stream,
        view: p4Config.view
      ]
    ]
  )
  // write the changelist we just synced back to the global changelist var
  currentChangelist = env.P4_CHANGELIST
  currentChangelist
}


// return the globally tracked p4 changelist for the current build
// returns empty string if we haven't synced yet
def currentP4Changelist(changelist) {
  // intentionally using currentChangelist from global scope
  if (changelist) {
    // allow manually overriding the globally set changelist
    currentChangelist = changelist
    echo "[INFO] [currentP4Changelist] Changelist was specified: ${changelist}"
    return currentChangelist
  }
  if (currentChangelist) {
    echo "[INFO] [currentP4Changelist] Changelist was previously set: ${currentChangelist}"
    return currentChangelist
  }
  echo "[INFO] [currentP4Changelist] Current changelist not found, will build most recent changelist."
  ''
}


String viewSpecWithWorkspace(viewspec, workspacePattern) {
  def p4Viewspec = ''
  if (viewspec) {
    // Allow viewspec to be specified as an array. If so, join and insert workspace name
    if ([Collection, Object[]].any { it.isAssignableFrom(viewspec.getClass()) }) {
      p4Viewspec = viewspec.join("\n")
    } else {
      p4Viewspec = viewspec
    }
    // __WORKSPACE__ is a magic string we find and replace so that pipelines don't
    // have to hardcode their workspace path
    p4Viewspec = p4Viewspec.replace("__WORKSPACE__", workspacePattern)
  }
  p4Viewspec
}

