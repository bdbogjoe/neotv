package net.cho20.neotv.script.service


import groovy.json.JsonBuilder
import groovy.xml.MarkupBuilder

import java.text.SimpleDateFormat

class Main {

    static void main(String[] args) {
        def cli = new CliBuilder(usage: 'groovy <script> [options]')
        cli._(longOpt: 'code', args: 1, argName: 'c', 'Set neotv code', required: true)
        cli._(longOpt: 'api', args: 1, argName: 'a', 'Set the api', required: true)
        cli._(longOpt: 'group', args: 1, argName: 'c', 'Set neotv group', required: false)
        cli._(longOpt: 'output', args: 1, argName: 'o', 'Set output', required: false)
        cli._(longOpt: 'format', args: 1, argName: 'o', 'Set output', required: true)


        def options = cli.parse(args)

        if(options){
            try {
                def processor = new Processor(options.code, options.api, options.group ?: null)
                def groups = processor.process().sort()
                if (options.output) {
                       def file = new File(options.output)
                       file.withWriter { writer ->
                           build(writer, options.format, groups)
                       }
                } else {
                    build(new OutputStreamWriter(System.out), options.format, groups)
                }
            }catch(Exception e){
                e.printStackTrace()
            }
        } else {
            System.exit(1)
        }
    }

    private static void build(Writer writer, String format, groups){
        if(format=='html'){
            def marker = new MarkupBuilder(writer)
            marker.html{
                head{
                    link(rel:"stylesheet",
                            href:"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css",
                            integrity:"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm",
                            crossorigin:"anonymous"){
                    }
                    title{
                        mkp.yield("NEO TV")
                    }
                    style{
                        mkp.yield('.video{border:1px dotted}')
                        mkp.yield('.group{border:2px solid}')
                    }

                }
                body{
                    div(class:'container') {
                        for (g in groups) {
                            div(class:'row group') {
                                div(class: 'col-12') {
                                    div(class:'row'){
                                        div(class:'col-12') {
                                            p(class: 'text-center') {
                                                mkp.yield(g.name)
                                            }
                                        }
                                    }
                                    for(Iterator i=g.videos.iterator();i.hasNext();){
                                        div(class:'row'){
                                            printVideo(marker, i.next())
                                            if(i.hasNext()) {
                                                printVideo(marker, i.next())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }else{
            JsonBuilder builder = new JsonBuilder(groups);
            builder.writeTo(writer)
        }
    }

    private static printVideo(marker, v){
        marker.div(class: 'col video') {
            p(class:'text-center') {
                a(href:v.url) {
                    mkp.yield(v.title)
                }
            }
            if (v.id_db) {
                div {
                    if (v.date) {
                        span {
                            mkp.yield('Date de sortie : '+new SimpleDateFormat('dd/MM/yyyy').format(v.date))
                        }
                    }
                }
                if(v.image) {
                    div {
                        img(src: "https://image.tmdb.org/t/p/w185/${v.image}")
                    }
                }
                if(v.overview) {
                    div {
                        span {
                            mkp.yield(v.overview)
                        }
                    }
                }
            }
        }
    }
}
