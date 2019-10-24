package net.cho20.neotv.script.service;

import net.cho20.neotv.script.bean.Group;

public interface Processor {

    Iterable<Group> process() ;
}
