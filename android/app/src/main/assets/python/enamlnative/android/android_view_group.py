'''
Copyright (c) 2017, Jairus Martin.

Distributed under the terms of the MIT License.

The full license is in the file COPYING.txt, distributed with this software.

Created on May 20, 2017

@author: jrm
'''
from atom.api import Typed, set_default

from enamlnative.widgets.view_group import ProxyViewGroup

from .android_view import AndroidView, View, MarginLayoutParams


class Gravity:
    NO_GRAVITY = 0
    CENTER_HORIZONTAL = 1
    CENTER_VERTICAL = 16
    CENTER = 11
    FILL = 119
    FILL_HORIZONTAL = 7
    FILL_VERTICAL = 112
    TOP = 48
    BOTTOM = 80
    LEFT = 3
    RIGHT = 5
    START = 8388611
    END = 8388613


class ViewGroup(View):
    __javaclass__ = set_default('android.view.ViewGroup')


class AndroidViewGroup(AndroidView, ProxyViewGroup):
    """ An Android implementation of an Enaml ProxyViewGroup.

    """
    #: A reference to the widget created by the proxy.
    widget = Typed(ViewGroup)

    # --------------------------------------------------------------------------
    # Initialization API
    # --------------------------------------------------------------------------
    def create_widget(self):
        """ Create the underlying label widget.

        """
        self.widget = ViewGroup(self.get_context())

    def init_widget(self):
        """ Initialize the underlying widget.

        """
        super(AndroidViewGroup, self).init_widget()
        d = self.declaration
        if d.layout_gravity:
            self.set_layout_gravity(d.layout_gravity)

    # --------------------------------------------------------------------------
    # ProxyViewGroup API
    # --------------------------------------------------------------------------
    def set_layout_gravity(self, gravity):
        g = getattr(Gravity, gravity.upper()) #3 if c.declaration.layout_gravity == 'left' else 5
        self.layout_params.gravity = g
