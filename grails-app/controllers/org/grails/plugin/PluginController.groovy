package org.grails.plugin

import org.grails.tags.TagNotFoundException

class PluginController {

    def pluginService
    def tagService

    def list() {
        def plugins = []
        def pluginCount = 0
        def maxResults = params.int('max') ?: 10
        def offset = params.int('offset') ?: 0

        def filter = params.filter ? params.filter.toString() : null
        if (filter) {
            (plugins, pluginCount) = pluginService."list${filter}PluginsWithTotal"(max: maxResults, offset: offset)
        }
        else {
            (plugins, pluginCount) = pluginService.listNewestPluginsWithTotal(max: maxResults, offset: offset)
        }

        def tags = tagService.getPluginTagArray()
        [ tags: tags, plugins: plugins, pluginCount: pluginCount ]
    }

    def listByTag() {
        try {
            def tags = tagService.getPluginTagArray()
            def maxResults = params.int('max') ?: 10
            def offset = params.int('offset') ?: 0
            def (plugins, pluginCount) = pluginService.listPluginsByTagWithTotal(params.tag, max: maxResults, offset: offset)
            render view: 'list', model: [
                    tags: tags,
                    plugins: plugins,
                    pluginCount: pluginCount,
                    max: maxResults,
                    offset: offset
            ]
        }
        catch (TagNotFoundException ex) {
            flash.message = "Tag not found"
            redirect action: 'list'
        }
    }

    def plugin() {
        def plugin = Plugin.findByName(params.id)
        def tags = tagService.getPluginTagArray()
        [ plugin: plugin, tags: tags ]
    }

    def submitPlugin() {
        def pluginPendingApproval = new PluginPendingApproval(
            user: request.user,
            status: ApprovalStatus.PENDING
        )
        if (request.method == "POST") {
            pluginPendingApproval.name = params.name
            pluginPendingApproval.scmUrl = params.scmUrl
            pluginPendingApproval.email = params.email
            pluginPendingApproval.notes = params.notes
            if (!pluginPendingApproval.hasErrors() && pluginPendingApproval.save(flush:true)) {
                flash.message = "Your plugin has been submitted for approval"
                pluginPendingApproval = new PluginPendingApproval(user: request.user)
                params.clear()
            } else {
                flash.message = "Please correct the fields below"
            }
        }
        [pluginPendingApproval: pluginPendingApproval]
    }

}