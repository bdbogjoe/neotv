package net.cho20.neotv.core.service;

import net.cho20.neotv.core.bean.Group;
import net.cho20.neotv.core.bean.Stream;

public interface Processor {

    Iterable<Group<Stream>> process() ;
}
