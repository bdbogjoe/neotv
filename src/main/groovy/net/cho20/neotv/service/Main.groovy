package net.cho20.neotv.service

import groovy.json.JsonBuilder

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
                def groups = processor.process()
                def out
                if (options.format == 'json') {
                    JsonBuilder builder = new JsonBuilder(groups);
                    out = builder.toPrettyString()
                }
                if (options.output) {

                } else {
                    println out
                }
            }catch(Exception e){
                e.printStackTrace()
            }
        } else {
            System.exit(1)
        }
    }
}
